import answer.king.Application;
import answer.king.model.Item;
import answer.king.model.LineItem;
import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.repo.ItemRepository;
import answer.king.repo.LineItemRepository;
import answer.king.repo.OrderRepository;
import answer.king.repo.ReceiptRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/*
TODO: If I had more time, I would ...
    1. Find alternative for spring.jackson.serialization.fail_on_empty_beans=false
    2. Add proper exceptions returning appropriate HTTP codes
    3. Other issues tagged with "T0DO"
    4. Check with Sonar for issues, coding standards and code coverage
    5. Setup same code formatting as in given code
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class OrderTest {
    private static final String TEST_VALUE_ITEM1_NAME = "TestOswald111";
    private static final String TEST_VALUE_ITEM2_NAME = "TestOswald222";
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Order order;
    private Long testItemId;
    private Long testItemId2;
    private Long testItemId3;
    private Long testLineItemId;
    private Long testOrderId;

    private List<Order> orderList = new ArrayList<>();
    private List<Receipt> receiptList = new ArrayList<>();

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        this.receiptRepository.deleteAll();

        this.orderList.clear();
        this.orderRepository.deleteAll();

        Order order1 = new Order();
        order1.setPaid(false);
        this.order = orderRepository.save(order1);

        Item item1 = new Item();
        item1.setName(TEST_VALUE_ITEM1_NAME);
        item1.setPrice(new BigDecimal("1.50"));

        Item item2 = new Item();
        item2.setName(TEST_VALUE_ITEM2_NAME);
        item2.setPrice(new BigDecimal("3.50"));

        Item item3 = new Item();
        item3.setName("TestOswald333");
        item3.setPrice(new BigDecimal("5.50"));

        LineItem lineItem = new LineItem();
        lineItem.setCurrentPrice(new BigDecimal(2));
        lineItem.setQuantity(1);
        lineItem.setItem(item1);
        lineItem.setOrder(this.order);

        LineItem lineItem2 = new LineItem();
        lineItem2.setCurrentPrice(new BigDecimal(2));
        lineItem2.setQuantity(1);
        lineItem2.setItem(item2);
        lineItem2.setOrder(this.order);

        testItemId = itemRepository.save(item1).getId();
        testItemId2 = itemRepository.save(item2).getId();
        testItemId3 = itemRepository.save(item3).getId();
        testLineItemId = lineItemRepository.save(lineItem).getId();
        testLineItemId = lineItemRepository.save(lineItem2).getId();

        List<LineItem> lineItemList = new ArrayList<>();
        lineItemList.add(lineItem);
        lineItemList.add(lineItem2);

        this.order.setLineItems(lineItemList);
        testOrderId = order.getId();

        Receipt receipt = new Receipt();
        receipt.setPayment(new BigDecimal("6"));
        receipt.setOrder(order);
        this.receiptList.add(receiptRepository.save(receipt));

        this.order = orderRepository.save(order);

        this.orderList.add(order);
    }


    @Test
    public void testShouldThrowExceptionWhenAmountNotEnough() throws Exception {
        setup();
        Long id = orderList.get(0).getId();

        assertEquals(orderRepository.findOne(id).getPaid(), false);

        this.mockMvc.perform(put("/order/" + id + "/pay")
                .contentType(contentType)
                .content("1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testShouldPassWhenItemAmountEqualToPayAmount() throws Exception {
        setup();
        Long id = orderList.get(0).getId();

        assertEquals(orderRepository.findOne(id).getPaid(), false);

        this.mockMvc.perform(put("/order/" + id + "/pay")
                .contentType(contentType)
                .content("5"))
                .andExpect(status().isOk());
    }

    @Test
    public void testShouldChangeFlagWhenPaid() throws Exception {
        setup();

        Long id = orderList.get(0).getId();

        assertEquals(orderRepository.findOne(id).getPaid(), false);

        this.mockMvc.perform(put("/order/" + id + "/pay")
                .contentType(contentType)
                .content("6"))
                .andExpect(status().isOk());

        assertEquals(orderRepository.findOne(id).getPaid(), true);
    }

    @Test
    public void testShouldRetainReceiptWhenPaid() throws Exception {
        setup();

        Long id = orderList.get(0).getId();
        double paymentAmount = 1234.00;

        assertEquals(orderRepository.findOne(id).getPaid(), false);

        String resultJson = this.mockMvc.perform(put("/order/" + id + "/pay")
                .contentType(contentType)
                .content(Double.toString(paymentAmount)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id_receipt = JsonPath.parse(resultJson).read("$.id", Long.class);

        assertEquals(receiptRepository.findOne(id_receipt).getPayment().compareTo(new BigDecimal(paymentAmount)), 0);


    }


    @Test
    public void testShouldShowExpectedOrderWhenRequested() throws Exception {
        setup();

        mockMvc.perform(get("/order"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].paid", is(false)))
            .andExpect(jsonPath("$[0].lineItems[0].item.name", is(TEST_VALUE_ITEM1_NAME)))
            .andExpect(jsonPath("$[0].lineItems[1].item.name", is(TEST_VALUE_ITEM2_NAME)));
    }


    @Test
    public void testShouldShowSameReceiptPriceWhenPriceIsUpdated() throws Exception {
        setup();

        Long order_id = testOrderId;
        Long item_id = testItemId;
        Integer price = 152;

        //Get receipt
        String resultJson = this.mockMvc.perform(put("/order/" + order_id + "/pay")
                .contentType(contentType)
                .content(json(price)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        BigDecimal totalOrderPriceBeforeChange = JsonPath.parse(resultJson).read("$.totalOrderPrice", BigDecimal.class);

        //Change price
        this.mockMvc.perform(put("/item/" + item_id + "/changeprice")
                .contentType(contentType)
                .content(json(price)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(price)))
                .andReturn().getResponse().getContentAsString();

        //Get same receipt and check price - should be the same
        //TODO Improve this, because it's not most efficient way
        String resultJson2 = this.mockMvc.perform(put("/order/" + order_id + "/pay")
                .contentType(contentType)
                .content(json("134564")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        BigDecimal totalOrderPriceAfterChange = JsonPath.parse(resultJson2).read("$.totalOrderPrice", BigDecimal.class);

        assertEquals(totalOrderPriceBeforeChange.compareTo(totalOrderPriceAfterChange), 0);

    }

    @Test
    public void testShouldCreateOrderWhenCorrectDataProvided() throws Exception {
        Item item1 = new Item();
        item1.setName("TestOswald1");
        item1.setPrice(new BigDecimal("3.03"));

        Item item2 = new Item();
        item2.setName("TestOswald2");
        item2.setPrice(new BigDecimal("1.03"));

        Order order1 = new Order();
        order1.setPaid(true);

        String orderJson = this.json(order1);

        this.mockMvc.perform(post("/order")
                .contentType(contentType)
                .content(orderJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testShouldUpdateQuantityWhenSameItemAdded() throws Exception {
        this.mockMvc.perform(put("/order/" + testOrderId + "/addItem/" + testItemId + "/1")
                .contentType(contentType)
                .content(""))
                .andExpect(status().isOk());

        Integer sizeBeforeAddingItem = lineItemRepository.findAll().size();
        Integer totalQuantityBeforeAddingItem = lineItemRepository.findAll().stream().map(LineItem::getQuantity).reduce(0, (a, b) -> a + b);

        Integer addedQuantity = 3;
        this.mockMvc.perform(put("/order/" + testOrderId + "/addItem/" + testItemId + "/" + addedQuantity)
                .contentType(contentType)
                .content(""))
                .andExpect(status().isOk());

        Integer sizeAfterAddingItem = lineItemRepository.findAll().size();
        Integer totalQuantityAfterAddingItem = lineItemRepository.findAll().stream().map(LineItem::getQuantity).reduce(0, (a, b) -> a + b);

        assertEquals(sizeBeforeAddingItem, sizeAfterAddingItem);
        assertTrue(totalQuantityAfterAddingItem - totalQuantityBeforeAddingItem == addedQuantity);
    }

    @Test
    public void testShouldAddNewItemWhenItemIsNotThere() throws Exception {
        this.mockMvc.perform(put("/order/" + testOrderId + "/addItem/" + testItemId + "/1")
                .contentType(contentType)
                .content(""))
                .andExpect(status().isOk());

        Integer sizeBeforeAddingItem = lineItemRepository.findAll().size();

        this.mockMvc.perform(put("/order/" + testOrderId + "/addItem/" + testItemId3 + "/3")
                .contentType(contentType)
                .content(""))
                .andExpect(status().isOk());

        Integer sizeAfterAddingItem = lineItemRepository.findAll().size();

        assertTrue(sizeBeforeAddingItem < sizeAfterAddingItem);
    }

    //TODO Refactor this to be reusable within other tests instead of copy/paste
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}