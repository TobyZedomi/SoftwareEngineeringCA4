package model;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class Order {

    private String username;
    private String buyerOrSeller;
    private String title;
    private double price;
    private int quantity;
    private LocalDateTime dateOfOrder;
}
