import answer.king.Application;
import answer.king.model.Item;
import answer.king.repo.ItemRepository;
import answer.king.repo.ReceiptRepository;
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
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ItemTest {
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;
    private static final String TEST_VALUE_ITEM1_NAME = "TestOswaldItem11111";

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Item item;

    private List<Item> itemList = new ArrayList<>();

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

        this.itemRepository.deleteAllInBatch();

        Item item1 = new Item();
        item1.setName(TEST_VALUE_ITEM1_NAME);
        item1.setPrice(new BigDecimal("0.03"));

        this.item = itemRepository.save(item1);
        this.itemList.add(item);
    }

    @Test
    public void testShouldShowExpectedItemWhenRequestedByApi() throws Exception {
        mockMvc.perform(get("/item"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(item.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(TEST_VALUE_ITEM1_NAME)))
                .andExpect(jsonPath("$[0].price", is(0.03)));
    }

    @Test
    public void testShouldChangePriceWhenRequestSentAndCorrectDataProvided() throws Exception {
        Double price = 15.00;
        Long id = itemList.get(0).getId();

        this.mockMvc.perform(put("/item/" + id + "/changeprice")
                .contentType(contentType)
                .content(json(price)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(price)));
    }

    @Test
    public void testShouldCreateItemAddNewDataWhenApiRequestSent() throws Exception {
        Item item1 = new Item();
        item1.setName("AnyRandomName");
        item1.setPrice(new BigDecimal("1.03"));

        String itemJson = this.json(item1);

        this.mockMvc.perform(post("/item")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testShouldThrowBadRequestWhenCreatingItemAndPriceIsInvalid() throws Exception {
        Item item1 = new Item();
        item1.setName("AnyAnyAnyRandomName98532");
        item1.setPrice(new BigDecimal("0"));

        String itemJson = this.json(item1);

        this.mockMvc.perform(post("/item")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testShouldThrowBadRequestWhenCreatingItemAndNameIsInvalid() throws Exception {
        Item item1 = new Item();
        item1.setName("Te");
        item1.setPrice(new BigDecimal("0.03"));

        String itemJson = this.json(item1);

        this.mockMvc.perform(post("/item")
                .contentType(contentType)
                .content(itemJson))
                .andExpect(status().isBadRequest());
    }

    //TODO Refactor this to be reusable within other tests instead of copy/paste
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}