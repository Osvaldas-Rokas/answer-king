package answer.king.controller;

import answer.king.model.Item;
import answer.king.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static answer.king.utils.Validations.validateName;
import static answer.king.utils.Validations.validatePrice;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Item> getAll() {
        return itemService.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public Item create(@RequestBody Item item) {
        validateName(item);
        validatePrice(item);
        return itemService.save(item);
    }

    @RequestMapping(value = "/{id}/changeprice", method = RequestMethod.PUT)
    public Item changePrice(@PathVariable("id") Long id, @RequestBody BigDecimal price) {
        return itemService.changePrice(id, price);
    }

}
