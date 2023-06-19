package skim.sfg.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import skim.sfg.beer.inventory.service.config.JmsConfig;
import skim.sfg.brewery.model.events.DeallocateOrderRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeallocationListener {

    private final AllocationService allocationService;

    @JmsListener(destination = JmsConfig.DEALLOCATE_ORDER_QUEUE)
    public void listen(DeallocateOrderRequest request) {
        allocationService.deallocateOrder(request.getBeerOrderDto());
    }
}
