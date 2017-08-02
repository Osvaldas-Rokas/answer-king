package answer.king.utils;

import answer.king.model.Item;

import java.math.BigDecimal;

public class Validations {
    public static void validatePrice(Item item) {
        if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            //      throw new InvalidProvidedDataException("Price is zero or less");
        }
    }

    public static void validateName(Item item) {
        if (item.getName().length() < 4) {
            //   throw new InvalidProvidedDataException("Provided name too short");
        }
    }


}
