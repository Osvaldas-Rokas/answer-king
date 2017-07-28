package answer.king.model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "T_ORDER")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Boolean paid = false;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.ALL, CascadeType.PERSIST})
    private List<LineItem> lineItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return String.format(
                "Order[id=%d, paid='%s', lineItems='%s']",
                id, paid, Arrays.toString(lineItems.toArray()));
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

}
