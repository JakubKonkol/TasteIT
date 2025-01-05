package pl.jakubkonkol.tasteitserver.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public Caffeine userCacheConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();
    }

    @Bean
    public Caffeine postCacheConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .initialCapacity(200)
                .maximumSize(2000)
                .recordStats();
    }

    @Bean
    public Caffeine tagCacheConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .initialCapacity(50)
                .maximumSize(500)
                .recordStats();
    }

    @Bean
    public Caffeine ingredientCacheConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(
            Caffeine userCacheConfig,
            Caffeine postCacheConfig,
            Caffeine tagCacheConfig,
            Caffeine ingredientCacheConfig
    ) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        
        // User-related caches
        caffeineCacheManager.registerCustomCache("userById", userCacheConfig.build());
        caffeineCacheManager.registerCustomCache("userBySessionToken", userCacheConfig.build());
        caffeineCacheManager.registerCustomCache("userProfileView", userCacheConfig.build());
        caffeineCacheManager.registerCustomCache("userShort", userCacheConfig.build());
        caffeineCacheManager.registerCustomCache("followers", userCacheConfig.build());
        caffeineCacheManager.registerCustomCache("following", userCacheConfig.build());
        
        // Post-related caches
        caffeineCacheManager.registerCustomCache("posts", postCacheConfig.build());
        caffeineCacheManager.registerCustomCache("postById", postCacheConfig.build());
        caffeineCacheManager.registerCustomCache("userPosts", postCacheConfig.build());
        caffeineCacheManager.registerCustomCache("postsByTag", postCacheConfig.build());
        caffeineCacheManager.registerCustomCache("likedPosts", postCacheConfig.build());
        caffeineCacheManager.registerCustomCache("postsAll", postCacheConfig.build());

        // Tag-related caches
        caffeineCacheManager.registerCustomCache("tags", tagCacheConfig.build());
        caffeineCacheManager.registerCustomCache("basicTags", tagCacheConfig.build());
        
        // Ingredient-related caches
        caffeineCacheManager.registerCustomCache("ingredients", ingredientCacheConfig.build());
        caffeineCacheManager.registerCustomCache("ingredientsById", ingredientCacheConfig.build());
        caffeineCacheManager.registerCustomCache("ingredientsAll", ingredientCacheConfig.build());
        caffeineCacheManager.registerCustomCache("ingredientsPages", ingredientCacheConfig.build());

        return caffeineCacheManager;
    }
} 