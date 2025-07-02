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

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuctionController {

    private final ItemRepository itemRepository;
    private final PictureService pictureService;
    private final FavoriteService favoriteService;
    private final StalcraftApiService stalcraftApiService;

    /**
     * Метод для отображения страницы списка избранного и поисковой строки
     *
     * @param search             введённая пользователем строка, по которой идёт поиск в бд
     * @param authentication     {@link Authentication} данные аутентифицированного пользователя
     * @param redirectAttributes для добавления в модель флеш атрибутов
     * @param model              так же для работы с атрибутами
     * @return {@code auction.html} но уже с найденными совпадениями по поиску
     */
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

        Set<Item> favoriteItems = favoriteService.getFavoriteItems(authentication);
        redirectAttributes.addFlashAttribute("favoriteItems", favoriteItems);

        model.addAttribute("favoriteItems", favoriteService.getFavoriteItems(authentication));
        model.addAttribute("items", items);
        model.addAttribute("search", search);

        return "auction";
    }

    /**
     * Метод для отображения страницы предмета, списка лотов и истории
     *
     * @param sortBy порядок сортировки списка лотов
     * @param order  по возрастанию/убыванию
     * @param itemId id предмета, передаётся как переменная пути
     * @param model  модель для работы с атрибутами
     * @return {@code item.html} стандартно загружает текущие лоты и сортирует по цене за штуку
     */
    @GetMapping("/auction/{itemId}")
    public String itemPage(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @PathVariable String itemId,
            Model model
    ) {
        ApiLotsDto itemLots = stalcraftApiService.getAuctionApiResponse(itemId, sortBy, order);
        Item item = itemRepository.findByApiId(itemId).orElse(null);
        String pictureUrl = pictureService.getPicture(item);

        model.addAttribute("itemLots", itemLots);
        model.addAttribute("item", item);
        model.addAttribute("pictureUrl", pictureUrl);

        model.addAttribute("itemIdHolder", item.getApiId());

        return "item";
    }

    /**
     * Метод для получения от апи списка записей о продажах предмета
     *
     * @param itemId айди предмета, по которому будет происходить запрос к апи
     * @param count  количество записей, которое будет возвращено, от 20 до 200, стандартно 20
     * @return {@link ApiHistoryDto} дто, содержащее количество лотов и их список {@code List<HistoryDto>} {@link ru.manakin.aucmonitor.dto.HistoryDto}
     */
    @GetMapping("/auction/{itemId}/history")
    @ResponseBody
    public ApiHistoryDto getPriceHistory(
            @PathVariable String itemId,
            @RequestParam(defaultValue = "20") String count) {
        return stalcraftApiService.getPriceHistoryResponse(itemId, count);
    }


    /**
     * Метод для вызова логики добавления предмета в избранное
     *
     * @param itemId             id предмета
     * @param authentication     {@link Authentication} данные аутентифицированного пользователя
     * @param redirectAttributes атрибуты для добавления в модель флеш атрибутов
     * @return {@code auction/item.apiId} возвращает страницу предмета, с добавленным уведомлением о добавлении предмета в избранное
     */
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

    /**
     * Метод для вызова логики удаления предмета из списка избранного пользователя
     *
     * @param itemId             id предмета
     * @param authentication     {@link Authentication} данные аутентифицированного пользователя
     * @param redirectAttributes атрибуты для добавления в модель флеш атрибутов
     * @return {@code auction/item.apiId} возвращает страницу предмета, с добавленным уведомлением об
     * удалении предмета из избрранного
     */
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
