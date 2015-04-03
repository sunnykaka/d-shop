package order.dtos;

import order.constants.OrderItemStatus;
import order.constants.OrderStatus;
import order.constants.OrderType;
import org.joda.time.DateTime;

/**
 * Created by liubin on 15-4-3.
 */
public class OrderSearcher {

    public Integer orderNo;

    public OrderStatus status;

    public OrderType type;

    public DateTime createTimeStart;

    public DateTime createTimeEnd;

    public OrderItemStatus orderItemStatus;

    public String productSku;

}
