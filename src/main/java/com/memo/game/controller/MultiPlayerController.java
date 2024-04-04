package com.memo.game.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Controller
@RequestMapping("/api/multiplayer")
public class MultiPlayerController {

    @GetMapping
    public ModelAndView index() {
        return memoGame(8);
    }

    @RequestMapping("/index")
    public ModelAndView memoGame(@RequestBody Integer numOfPairs) {
        ModelAndView modelAndView = new ModelAndView("index");
        Integer[] board = new Integer[numOfPairs];
        modelAndView.addObject("board", board);
        return modelAndView;
    }
}
