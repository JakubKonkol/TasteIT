package pl.jakubkonkol.tasteitserver.service;

import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.jakubkonkol.tasteitserver.dto.IngredientDto;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import pl.jakubkonkol.tasteitserver.model.projection.IngredientSearchView;
import pl.jakubkonkol.tasteitserver.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.service.interfaces.IIngredientService;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class IngredientService implements IIngredientService {
    private final IngredientRepository ingredientRepository;
    private final ModelMapper modelMapper;

    @Cacheable("ingredients")
    public Optional<Ingredient> findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        return ingredientRepository.findByName(name);
    }

    public IngredientDto getIngredient(String ingredientId) {
        if (ingredientId == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }
        var ingredient = ingredientRepository.findById(ingredientId).orElse(null);
        if(ingredient == null){
            throw new ResourceNotFoundException("Ingredient not found with Id: " + ingredientId);
        }
        return convertToDto(ingredient);
    }

    public IngredientDto save(Ingredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient cannot be null.");
        }
        if(ingredientRepository.findByNameIgnoreCase(ingredient.getName()).isEmpty()){
            ingredientRepository.save(ingredient);
        }

        return convertToDto(ingredient);
    }
    public List<IngredientDto> saveAll(List<Ingredient> ingredients) {
        if (ingredients == null) {
            throw new IllegalArgumentException("List of drinks cannot be null.");
        }
        ingredients.forEach(ingredient -> {
            if (ingredient == null) {
                throw new IllegalArgumentException("Ingredient cannot be null.");
            }
            save(ingredient);
        });

        return ingredients.stream().map(i -> convertToDto(i)).toList();
    }
    public void deleteAll() {
        if (ingredientRepository.count() == 0) {
            throw new IllegalStateException("No ingredients to delete.");
        }
        ingredientRepository.deleteAll();
    }

    public void deleteById(String ingredientId) {
        if (ingredientId == null || ingredientId.isEmpty()) {
            throw new IllegalArgumentException("Ingredient ID cannot be null or empty.");
        }
        if (!ingredientRepository.existsById(ingredientId)) {
            throw new IllegalStateException("Ingredient with ID " + ingredientId + " does not exist.");
        }
        ingredientRepository.deleteById(ingredientId);
    }

    public List<IngredientDto> getAll() {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        return ingredients.stream()
                .map(this::convertToDto)
                .toList();
    }

    //na razie nie potrzebna
//    public List<IngredientDto> searchByName(String name) {
//        if (name == null) {
//            throw new IllegalArgumentException("Name cannot be null.");
//        }
//        List <Ingredient> ingredientList = ingredientRepository.findIngredientByNameContainingIgnoreCase(name);
//        return ingredientList.stream().map(this::convertToDto).toList();
//
//    }

    public PageDto<IngredientDto> searchIngredientsByName(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<IngredientSearchView> ingredients = ingredientRepository.findByNameContainingIgnoreCase(name, pageable);

        return getIngredientDtoFromIngredientSearchView(ingredients, pageable);
    }

    private static PageDto<IngredientDto> getIngredientDtoFromIngredientSearchView(Page<IngredientSearchView> ingredients, Pageable pageable) {
        List<IngredientDto> ingredientDtos = ingredients.getContent().stream().map(i -> {
            IngredientDto ingredientDto = new IngredientDto();
            ingredientDto.setIngredientId(i.getIngredientId());
            ingredientDto.setName(i.getName());
            return ingredientDto;
        }).toList();

        PageImpl<IngredientDto> pageImpl = new PageImpl<>(ingredientDtos, pageable, ingredients.getTotalElements());
        return getIngredientDtoPageDto(pageImpl);
    }

    private static PageDto<IngredientDto> getIngredientDtoPageDto(PageImpl<IngredientDto> pageImpl) {
        PageDto<IngredientDto> pageDto = new PageDto<>();
        pageDto.setContent(pageImpl.getContent());
        pageDto.setPageNumber(pageImpl.getNumber());
        pageDto.setPageSize(pageImpl.getSize());
        pageDto.setTotalElements(pageImpl.getTotalElements());
        pageDto.setTotalPages(pageImpl.getTotalPages());

        return pageDto;
    }

    public IngredientDto convertToDto(Ingredient ingredient) {
        return modelMapper.map(ingredient, IngredientDto.class);
    }
}
