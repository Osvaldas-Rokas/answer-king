package answer.king.service;

import answer.king.model.Item;
import answer.king.repo.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static answer.king.utils.Validations.validatePrice;

@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAll() {
        return itemRepository.findAll();
    }

    public Item changePrice(Long id, BigDecimal price) {
        Item item = itemRepository.findOne(id);

        validatePrice(item);
        item.setPrice(price);
        return itemRepository.save(item);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }
}
