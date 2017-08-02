package answer.king.controller;

import answer.king.exceptions.InvalidProvidedDataException;
import answer.king.model.Item;
import answer.king.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
        //   validateName(item);
        //   validatePrice(item);
        try {
            return itemService.save(item);
        } catch (Exception e) {
            throw new InvalidProvidedDataException("Provided data is invalid");
        }
    }

    @RequestMapping(value = "/{id}/changeprice", method = RequestMethod.PUT)
    public Item changePrice(@PathVariable("id") Long id, @RequestBody BigDecimal price) {
        return itemService.changePrice(id, price);
    }

}
