package answer.king.service;

import answer.king.exceptions.InvalidProvidedDataException;
import answer.king.model.Item;
import answer.king.model.LineItem;
import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.repo.ItemRepository;
import answer.king.repo.OrderRepository;
import answer.king.repo.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public void addItem(Long id, Long itemId, Integer quantity) {
        Order order = orderRepository.findOne(id);
        Item item = itemRepository.findOne(itemId);

        LineItem lineItemFound = order.getLineItems().stream().filter(y -> Objects.equals(y.getItem().getId(), itemId)).findAny().orElse(null);
        if (lineItemFound != null) {
            Integer indexOfLineItem = order.getLineItems().indexOf(lineItemFound);
            Integer quantityBefore = lineItemFound.getQuantity();
            lineItemFound.setQuantity(quantityBefore + quantity);
            order.getLineItems().set(indexOfLineItem, lineItemFound);
        } else {
            LineItem lineItem = new LineItem();
            lineItem.setCurrentPrice(item.getPrice());
            lineItem.setQuantity(quantity);
            lineItem.setOrder(order);
            lineItem.setItem(item);

            List<LineItem> lineItems = new ArrayList<>();
            lineItems.add(lineItem);

            order.setLineItems(lineItems);
        }

        orderRepository.save(order);
    }

    public Receipt pay(Long id, BigDecimal payment) {
        Order order = orderRepository.findOne(id);

        //TODO Check if paid to avoid paying twice

        if (order != null) {
            //I like Scala and functional programming. Java 8 is also getting there
            BigDecimal totalSum = order.getLineItems().stream().map(LineItem::getCurrentPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalSum.compareTo(payment) <= 0) {
                order.setPaid(true);
            } else {
                //Throw correct HTTP code when amount isnot enough
                throw new InvalidProvidedDataException(""); //TODO Fix to throw appropriate HTTP code
            }
        } else {
            throw new InvalidProvidedDataException(""); //TODO Fix to throw appropriate HTTP code
        }
        Receipt receipt = new Receipt();
        receipt.setPayment(payment);
        receipt.setOrder(order);
        receiptRepository.save(receipt);

        return receipt;
    }
}
