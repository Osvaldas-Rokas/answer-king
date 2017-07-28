package answer.king.controller;

import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Order> getAll() {
        return orderService.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public Order create() {
        return orderService.save(new Order());
    }

    @RequestMapping(value = "/{id}/addItem/{itemId}", method = RequestMethod.PUT)
    public void addItem(@PathVariable("id") Long id, @PathVariable("itemId") Long itemId) {
        orderService.addItem(id, itemId, 1);
    }

    @RequestMapping(value = "/{id}/addItem/{itemId}/{quantity}", method = RequestMethod.PUT)
    public void addItem(@PathVariable("id") Long id, @PathVariable("itemId") Long itemId, @PathVariable("quantity") Integer quantity) {
        orderService.addItem(id, itemId, quantity);
    }

    @RequestMapping(value = "/{id}/pay", method = RequestMethod.PUT)
    public Receipt pay(@PathVariable("id") Long id, @RequestBody BigDecimal payment) {
        return orderService.pay(id, payment);
    }


}
