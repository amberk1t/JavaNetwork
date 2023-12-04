package org.top.onlinestoreapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.top.onlinestoreapi.entity.Item;
import org.top.onlinestoreapi.service.ItemService;

import java.util.Optional;

@Controller
@RequestMapping("item")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService smartphoneService) {
        this.itemService = smartphoneService;
    }

    @GetMapping("")
    public String getAll(Model model) {
        Iterable<Item> smartphones = itemService.getAll("smartphone");
        Iterable<Item> tvs = itemService.getAll("tv");
        Iterable<Item> laptops = itemService.getAll("laptop");
        model.addAttribute("smartphones", smartphones);
        model.addAttribute("tvs", tvs);
        model.addAttribute("laptops", laptops);
        return "item/item-all";
    }

    @GetMapping("/smartphone")
    public String getAllSmart(Model model) {
        String type = "smartphone";
        Iterable<Item> smartphones = itemService.getAll(type);
        model.addAttribute("items", smartphones);
        return "item/item-all";
    }

    @GetMapping("/tv")
    public String getAllTv(Model model) {
        String type = "tv";
        Iterable<Item> tvs = itemService.getAll(type);
        model.addAttribute("items", tvs);
        return "item/item-all";
    }

    @GetMapping("/laptop")
    public String getAllLaptop(Model model) {
        String type = "laptop";
        Iterable<Item> laptops = itemService.getAll(type);
        model.addAttribute("items", laptops);
        return "item/item-all";
    }

    @GetMapping("new")
    public String getNew(Model model) {
        Item item = new Item();
        model.addAttribute("item", item);
        return "item/add-new";
    }

    @PostMapping("new")
    public String postNew(Item smartphone, RedirectAttributes redirectAttributes) {
        Optional<Item> add = itemService.addNew(smartphone);
        if (add.isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Товар " + add.get().getName() + " успешно добавлен"
                    );
        }
        return "redirect:/item";
    }

    @GetMapping("/delete/{id}")
    public String deleteById(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Item> deleted = itemService.deleteById(id);
        if (deleted.isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Товар " + deleted.get() + " успешно удален"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "dangerMessage",
                    "Товар с id " + id + " не найден"
            );
        }
        return "redirect:/item";
    }

    @GetMapping("/update/{id}")
    public String getUpdate(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Item> updated = itemService.getById(id);
        if (updated.isPresent()) {
            model.addAttribute("item", updated.get());
            return "item/update";
        } else {
            redirectAttributes.addFlashAttribute(
                    "dangerMessage",
                    "Товар с id " + id + " не найден"
            );
            return "redirect:/item";
        }
    }

    @PostMapping("/update")
    public String postUpdate(Item item, RedirectAttributes redirectAttributes) {
        Optional<Item> updated = itemService.update(item);
        if (updated.isPresent()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Товар " + updated.get().getName() + " успешно обновлен"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "dangerMessage",
                    "Товар не получилось обновить"
            );
        }
        return "redirect:/item";
    }

    @GetMapping("{id}")
    public String details(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Item> find = itemService.getById(id);
        if (find.isPresent()) {
            model.addAttribute("item", find.get());
            return "item/details";
        } else {
            redirectAttributes.addFlashAttribute(
                    "dangerMessage",
                    "Товар с id " + id + " не найден");

            return "redirect:/item";
        }

    }

}