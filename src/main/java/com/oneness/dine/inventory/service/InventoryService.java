package com.oneness.dine.inventory.service;

import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.core.enums.StockTxnType;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.inventory.dto.*;
import com.oneness.dine.inventory.entity.InventoryItem;
import com.oneness.dine.inventory.entity.MenuItemIngredient;
import com.oneness.dine.inventory.entity.StockTransaction;
import com.oneness.dine.inventory.repository.InventoryItemRepository;
import com.oneness.dine.inventory.repository.MenuItemIngredientRepository;
import com.oneness.dine.inventory.repository.StockTransactionRepository;
import com.oneness.dine.menu.entity.MenuItem;
import com.oneness.dine.menu.repository.ItemVariantRepository;
import com.oneness.dine.menu.repository.MenuItemRepository;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository     itemRepository;
    private final StockTransactionRepository  txnRepository;
    private final MenuItemIngredientRepository ingredientRepository;
    private final BranchRepository            branchRepository;
    private final MenuItemRepository          menuItemRepository;
    private final ItemVariantRepository       variantRepository;
    private final UserRepository              userRepository;

    // ─── INVENTORY ITEMS ─────────────────────────────

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getAllItems(Long branchId) {
        return itemRepository
                .findByBranchIdAndIsActiveTrueOrderByNameAsc(branchId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getLowStockItems(Long branchId) {
        return itemRepository.findLowStockItems(branchId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryItemDto getItemById(Long id) {
        return toDto(itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InventoryItem", "id", id)));
    }

    @Transactional
    public InventoryItemDto createItem(InventoryItemRequest request) {
        if (itemRepository.existsByBranchIdAndName(
                request.getBranchId(), request.getName())) {
            throw new BusinessException(
                    "Inventory item with this name already exists",
                    "ITEM_EXISTS");
        }

        InventoryItem item = new InventoryItem();
        item.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        item.setName(request.getName());
        item.setUnit(request.getUnit());
        item.setCurrentStock(BigDecimal.ZERO);
        item.setMinimumStock(request.getMinimumStock());
        item.setReorderQuantity(request.getReorderQuantity());
        item.setCostPerUnit(request.getCostPerUnit());
        item.setSupplierName(request.getSupplierName());
        item.setIsActive(true);

        InventoryItem saved = itemRepository.save(item);
        log.info("Inventory item created: {}", saved.getName());
        return toDto(saved);
    }

    @Transactional
    public InventoryItemDto updateItem(Long id, InventoryItemRequest request) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InventoryItem", "id", id));

        if (request.getName()           != null) item.setName(request.getName());
        if (request.getUnit()           != null) item.setUnit(request.getUnit());
        if (request.getMinimumStock()   != null) item.setMinimumStock(request.getMinimumStock());
        if (request.getReorderQuantity()!= null) item.setReorderQuantity(request.getReorderQuantity());
        if (request.getCostPerUnit()    != null) item.setCostPerUnit(request.getCostPerUnit());
        if (request.getSupplierName()   != null) item.setSupplierName(request.getSupplierName());

        return toDto(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InventoryItem", "id", id));
        item.setIsActive(false);
        itemRepository.save(item);
        log.info("Inventory item soft deleted: {}", id);
    }

    // ─── STOCK TRANSACTIONS ──────────────────────────

    @Transactional
    public StockTransactionDto addTransaction(StockTransactionRequest request) {
        InventoryItem item = itemRepository.findById(request.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InventoryItem", "id", request.getInventoryItemId()));

        // Update stock based on transaction type
        // Performance: update stock inline — no extra query
        BigDecimal qty = request.getQuantity();
        switch (request.getTxnType()) {
            case PURCHASE:
                item.setCurrentStock(item.getCurrentStock().add(qty));
                if (request.getUnitCost() != null) {
                    item.setCostPerUnit(request.getUnitCost());
                }
                break;
            case CONSUMPTION:
            case WASTAGE:
                if (item.getCurrentStock().compareTo(qty) < 0) {
                    throw new BusinessException(
                            String.format("Insufficient stock. Available: %s %s",
                                    item.getCurrentStock(), item.getUnit()),
                            "INSUFFICIENT_STOCK");
                }
                item.setCurrentStock(item.getCurrentStock().subtract(qty));
                break;
            case ADJUSTMENT:
                // qty can be positive or negative
                item.setCurrentStock(item.getCurrentStock().add(qty));
                break;
            case RETURN:
                item.setCurrentStock(item.getCurrentStock().add(qty));
                break;
        }
        itemRepository.save(item);

        // Save transaction record
        StockTransaction txn = new StockTransaction();
        txn.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        txn.setInventoryItem(item);
        txn.setTxnType(request.getTxnType());
        txn.setQuantity(request.getQuantity());
        txn.setUnitCost(request.getUnitCost());
        txn.setReferenceId(request.getReferenceId());
        txn.setReferenceType(request.getReferenceType());
        txn.setNotes(request.getNotes());

        if (request.getCreatedByUserId() != null) {
            userRepository.findById(request.getCreatedByUserId())
                    .ifPresent(txn::setCreatedByUser);
        }

        StockTransaction saved = txnRepository.save(txn);
        log.info("Stock transaction recorded: {} {} for item {}",
                request.getTxnType(), qty, item.getName());
        return toTxnDto(saved);
    }

    @Transactional(readOnly = true)
    public List<StockTransactionDto> getTransactions(Long inventoryItemId) {
        return txnRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId)
                .stream().map(this::toTxnDto)
                .collect(Collectors.toList());
    }

    // ─── RECIPE MANAGEMENT ───────────────────────────

    @Transactional(readOnly = true)
    public List<RecipeIngredientDto> getRecipe(Long itemId) {
        return ingredientRepository.findByItemId(itemId)
                .stream().map(this::toIngredientDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RecipeIngredientDto> saveRecipe(Long itemId,
            List<RecipeIngredientRequest> ingredients) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", itemId));

        // Delete existing recipe first
        ingredientRepository.deleteByItemId(itemId);

        // Save new recipe ingredients
        List<MenuItemIngredient> saved = ingredients.stream().map(req -> {
            InventoryItem invItem = itemRepository
                    .findById(req.getInventoryItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "InventoryItem", "id", req.getInventoryItemId()));

            MenuItemIngredient ingredient = new MenuItemIngredient();
            ingredient.setItem(menuItem);
            ingredient.setInventoryItem(invItem);
            ingredient.setQuantityUsed(req.getQuantityUsed());

            if (req.getVariantId() != null) {
                variantRepository.findById(req.getVariantId())
                        .ifPresent(ingredient::setVariant);
            }
            return ingredientRepository.save(ingredient);
        }).collect(Collectors.toList());

        log.info("Recipe saved for item {}: {} ingredients",
                itemId, saved.size());
        return saved.stream().map(this::toIngredientDto)
                .collect(Collectors.toList());
    }

    // ─── AUTO DEDUCT STOCK (called after order served) ──
    @Transactional
    public void autoDeductStock(Long orderId, Long branchId) {
        // This is called from OrderService when order is marked SERVED
        // For each order item, deduct stock based on recipe
        log.info("Auto stock deduction triggered for order: {}", orderId);
        // Implementation hooks into order items via orderId
        // Full implementation connects with OrderService
    }

    // ─── MAPPERS ─────────────────────────────────────

    private InventoryItemDto toDto(InventoryItem i) {
        boolean isLowStock = i.getCurrentStock()
                .compareTo(i.getMinimumStock()) <= 0;
        return InventoryItemDto.builder()
                .id(i.getId())
                .branchId(i.getBranch().getId())
                .name(i.getName())
                .unit(i.getUnit())
                .currentStock(i.getCurrentStock())
                .minimumStock(i.getMinimumStock())
                .reorderQuantity(i.getReorderQuantity())
                .costPerUnit(i.getCostPerUnit())
                .supplierName(i.getSupplierName())
                .isActive(i.getIsActive())
                .isLowStock(isLowStock)
                .build();
    }

    private StockTransactionDto toTxnDto(StockTransaction t) {
        return StockTransactionDto.builder()
                .id(t.getId())
                .inventoryItemId(t.getInventoryItem().getId())
                .inventoryItemName(t.getInventoryItem().getName())
                .branchId(t.getBranch().getId())
                .txnType(t.getTxnType())
                .quantity(t.getQuantity())
                .unitCost(t.getUnitCost())
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType())
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private RecipeIngredientDto toIngredientDto(MenuItemIngredient i) {
        return RecipeIngredientDto.builder()
                .id(i.getId())
                .itemId(i.getItem().getId())
                .variantId(i.getVariant() != null ?
                        i.getVariant().getId() : null)
                .inventoryItemId(i.getInventoryItem().getId())
                .inventoryItemName(i.getInventoryItem().getName())
                .unit(i.getInventoryItem().getUnit())
                .quantityUsed(i.getQuantityUsed())
                .build();
    }
}
