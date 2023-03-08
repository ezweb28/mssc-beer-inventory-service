package skim.sfg.beer.inventory.service.services;

import org.springframework.jms.annotation.JmsListener;
import skim.sfg.beer.inventory.service.config.JmsConfig;
import skim.sfg.beer.inventory.service.domain.BeerInventory;
import skim.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import skim.sfg.common.events.NewInventoryEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class NewInventoryListener {

    public final BeerInventoryRepository beerInventoryRepository;

    @JmsListener(destination = JmsConfig.NEW_INVENTORY_QUEUE)
    public void listen(NewInventoryEvent event) {
        log.debug("Got Inventory: " + event.toString());

        beerInventoryRepository.save(BeerInventory.builder()
                .beerId(event.getBeerDto().getId())
                .upc(event.getBeerDto().getUpc())
                .quantityOnHand(event.getBeerDto().getQuantityOnHand())
                .build());
    }
}
