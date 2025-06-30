package ru.manakin.aucmonitor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.manakin.aucmonitor.dto.ApiHistoryDto;
import ru.manakin.aucmonitor.dto.ApiLotsDto;
import ru.manakin.aucmonitor.model.Item;
import ru.manakin.aucmonitor.repository.ItemRepository;
import ru.manakin.aucmonitor.service.FavoriteService;
import ru.manakin.aucmonitor.service.PictureService;
import ru.manakin.aucmonitor.service.StalcraftApiService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuctionController {

    private final ItemRepository itemRepository;
    private final PictureService pictureService;
    private final FavoriteService favoriteService;
    private final StalcraftApiService stalcraftApiService;

    @GetMapping("/auction")
    public String searchItems(
            @RequestParam(required = false) String search,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        List<Item> items;

        if (search != null) {
            items = itemRepository.findByNameContainingIgnoreCase(search);
        } else {
            items = (List<Item>) itemRepository.findAll();
        }

        Set<Item> updatedFavorites = favoriteService.getFavoriteItems(authentication);
        redirectAttributes.addFlashAttribute("favoriteItems", updatedFavorites);

        model.addAttribute("favoriteItems", favoriteService.getFavoriteItems(authentication));
        model.addAttribute("items", items);
        model.addAttribute("search", search);
        return "auction";
    }

    @GetMapping("/auction/{itemId}")
    public String itemPage(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @PathVariable String itemId,
            Model model
    ) {
        sortBy = sortBy == null ? "buyout_price" : sortBy;
        order = order == null ? "asc" : order;

        ApiLotsDto itemLots = stalcraftApiService.getAuctionApiResponse(itemId, sortBy, order);


        switch (sortBy) {
            case "priceForOne":
                itemLots = stalcraftApiService.sortLotsByOnePiecePrice(itemLots);
                break;
            case "buyout_price":
                itemLots = stalcraftApiService.sortLotsByBuyoutPrice(itemLots);
                break;
            case "time_left":
                itemLots = stalcraftApiService.sortLotsByRemainingTime(itemLots);
                break;
            default:
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            Collections.reverse(itemLots.lots);
        }
        stalcraftApiService.adaptTimeFormat(itemLots);
        model.addAttribute("itemLots", itemLots);

        Item item = itemRepository.findByApiId(itemId).orElse(null);
        model.addAttribute("item", item);

        String pictureUrl = pictureService.getPicture(item);
        model.addAttribute("pictureUrl", pictureUrl);
        model.addAttribute("itemIdHolder", item.getApiId());

        return "item";
    }

    @GetMapping("/auction/{itemId}/history")
    @ResponseBody
    public ApiHistoryDto getPriceHistory(
            @PathVariable String itemId,
            @RequestParam(defaultValue = "20") String count) {
        return stalcraftApiService.getPriceHistoryResponse(itemId, count);
    }


    @PostMapping("/favorites")
    public String favorites(
            @RequestParam String itemId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Item item = itemRepository.findByApiId(itemId).orElse(null);

        favoriteService.addItemToFavorites(item, authentication);

        redirectAttributes.addFlashAttribute("added", true);
        redirectAttributes.addFlashAttribute("item", item);

        return "redirect:/auction/" + item.getApiId();
    }

    @PostMapping("/delfavorites")
    public String removeFromFavorites(
            @RequestParam String itemId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Item item = itemRepository.findByApiId(itemId).orElse(null);

        favoriteService.deleteItemFromFavorites(item, authentication);

        redirectAttributes.addFlashAttribute("deleted", true);
        redirectAttributes.addFlashAttribute("item", item);

        return "redirect:/auction/" + item.getApiId();
    }
}
