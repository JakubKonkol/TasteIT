package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.Tag;
import pl.jakubkonkol.tasteitserver.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;
    @PostMapping ("/")
    public void saveAll(@RequestBody List<Tag> tags) {
        tagService.saveAll(tags);
        System.out.println(tags);

    }
}
