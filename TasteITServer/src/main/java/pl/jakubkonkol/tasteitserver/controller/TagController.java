package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.model.Tag;
import pl.jakubkonkol.tasteitserver.service.interfaces.ITagService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tag")
public class TagController {
    private final ITagService tagService;

    @GetMapping("/basic")
    public ResponseEntity<List<Tag>> getBasicTags() {
        List<Tag> tags = tagService.getBasicTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/")
    public ResponseEntity<List<Tag>> getAllTags(){
        return ResponseEntity.ok(tagService.getAll());
    }

    @PostMapping("/")
    public ResponseEntity<Tag> saveTag(@Valid @RequestBody Tag tag){
        return ResponseEntity.ok(tagService.save(tag));
    }
}
