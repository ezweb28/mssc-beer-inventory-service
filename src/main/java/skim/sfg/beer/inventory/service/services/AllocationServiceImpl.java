package skim.sfg.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skim.sfg.beer.inventory.service.domain.BeerInventory;
import skim.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import skim.sfg.brewery.model.BeerOrderDto;
import skim.sfg.brewery.model.BeerOrderLineDto;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating OrderId: " + beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            if((((beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0)
                    - (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0)) > 0)) {
                allocateBeerOrderLine(beerOrderLineDto);
            }
            totalOrdered.set(totalOrdered.get() + beerOrderLineDto.getOrderQuantity());
            totalAllocated.set(totalAllocated.get() + (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0));
        });

        log.debug("Total Ordered: " + totalOrdered.get() + ". Total Allocated: " + totalAllocated.get());

        return totalOrdered.get() == totalAllocated.get();

    }

    @Override
    public void deallocateOrder(BeerOrderDto beerOrderDto) {
        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            BeerInventory beerInventory = BeerInventory.builder()
                    .beerId(beerOrderLineDto.getBeerId())
                    .upc(beerOrderLineDto.getUpc())
                    .quantityOnHand(beerOrderLineDto.getQuantityAllocated())
                    .build();

            BeerInventory savedInventory = beerInventoryRepository.save(beerInventory);

            log.debug("Saved Inventory for beer upc: " + savedInventory.getUpc() + " inventory id: " + savedInventory.getId());
        });
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());

        beerInventoryList.forEach(beerInventory -> {
            int inventory = (beerInventory.getQuantityOnHand() == null) ? 0 : beerInventory.getQuantityOnHand();
            int orderQty = (beerOrderLineDto.getOrderQuantity() == null) ? 0 : beerOrderLineDto.getOrderQuantity();
            int allocatedQty = (beerOrderLineDto.getQuantityAllocated() == null) ? 0 : beerOrderLineDto.getQuantityAllocated();
            int qtyToAllocate = orderQty - allocatedQty;

            if(inventory >= qtyToAllocate) { // full allocation
                inventory = inventory - qtyToAllocate;
                beerOrderLineDto.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if(inventory > 0) { // partial allocation
                beerOrderLineDto.setQuantityAllocated(allocatedQty + inventory);
                beerInventory.setQuantityOnHand(0);

            }

            if (beerInventory.getQuantityOnHand() == 0) {
                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
