package de.codecentric.psd.worblehat.web.controller;

import de.codecentric.psd.worblehat.domain.BookService;
import de.codecentric.psd.worblehat.web.formdata.ReturnAllBooksFormData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

/**
 * Controller class for the
 */
@Controller
@RequestMapping("/returnAllBooks")
@RequiredArgsConstructor
public class ReturnAllBooksController {

    @NonNull
    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET)
    public void prepareView(ModelMap modelMap) {
        modelMap.put("returnAllBookFormData", new ReturnAllBooksFormData());
    }

    @RequestMapping(method = RequestMethod.POST)
    public String returnAllBooks(
        @ModelAttribute("returnAllBookFormData") @Valid ReturnAllBooksFormData formData,
        BindingResult result) {
        if (result.hasErrors()) {
            return "returnAllBooks";
        } else {
            bookService.returnAllBooksByBorrower(formData.getEmailAddress());
            return "home";
        }
    }

}
