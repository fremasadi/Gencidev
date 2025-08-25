// app/src/main/java/com/example/gencidevtest/data/repository/ProductRepositoryImpl.kt
package com.example.gencidevtest.data.repository

import android.util.Log
import com.example.gencidevtest.data.local.dao.CategoryDao
import com.example.gencidevtest.data.local.dao.ProductDao
import com.example.gencidevtest.data.local.entity.toDomain
import com.example.gencidevtest.data.local.entity.toEntity
import com.example.gencidevtest.data.remote.api.ProductApiService
import com.example.gencidevtest.data.remote.dto.CategoryDto
import com.example.gencidevtest.data.remote.dto.ProductDto
import com.example.gencidevtest.data.util.NetworkUtil
import com.example.gencidevtest.domain.model.Category
import com.example.gencidevtest.domain.model.Product
import com.example.gencidevtest.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ProductApiService,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val networkUtil: NetworkUtil
) : ProductRepository {

    companion object {
        private const val TAG = "ProductRepositoryImpl"
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }

    override suspend fun getProducts(limit: Int, skip: Int): Result<List<Product>> {
        return try {
            Log.d(TAG, "Getting products with limit: $limit, skip: $skip")

            // First, try to get cached data
            val cachedProducts = productDao.getAllProductsSync()
            Log.d(TAG, "Found ${cachedProducts.size} cached products")

            // Check if we should use cache or fetch from network
            val shouldFetchFromNetwork = networkUtil.isNetworkAvailable() &&
                    (cachedProducts.isEmpty() || isCacheStale())

            if (shouldFetchFromNetwork) {
                Log.d(TAG, "Fetching products from network")

                try {
                    val response = apiService.getProducts(limit, skip)
                    val products = response.products.map { it.toDomain() }

                    // Cache the products
                    Log.d(TAG, "Caching ${products.size} products to local database")
                    productDao.replaceAllProducts(products.map { it.toEntity() })

                    Log.d(TAG, "Successfully fetched and cached ${products.size} products")
                    Result.success(products)
                } catch (e: Exception) {
                    Log.e(TAG, "Network request failed, using cached data if available", e)

                    if (cachedProducts.isNotEmpty()) {
                        Log.d(TAG, "Using ${cachedProducts.size} cached products as fallback")
                        Result.success(cachedProducts.map { it.toDomain() })
                    } else {
                        Result.failure(e)
                    }
                }
            } else {
                Log.d(TAG, "Using cached products (${cachedProducts.size} items)")
                Result.success(cachedProducts.map { it.toDomain() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getProducts", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductById(productId: Int): Result<Product> {
        return try {
            Log.d(TAG, "Getting product by ID: $productId")

            // First try to get from cache
            val cachedProduct = productDao.getProductById(productId)

            if (cachedProduct != null && !networkUtil.isNetworkAvailable()) {
                Log.d(TAG, "Using cached product (offline)")
                return Result.success(cachedProduct.toDomain())
            }

            // Try to fetch from network if available
            if (networkUtil.isNetworkAvailable()) {
                try {
                    val response = apiService.getProductById(productId)
                    val product = response.toDomain()

                    // Update cache
                    productDao.insertProduct(product.toEntity())
                    Log.d(TAG, "Successfully fetched and cached product: ${product.title}")

                    Result.success(product)
                } catch (e: Exception) {
                    Log.e(TAG, "Network request failed for product $productId", e)

                    // Fallback to cached version if available
                    if (cachedProduct != null) {
                        Log.d(TAG, "Using cached product as fallback")
                        Result.success(cachedProduct.toDomain())
                    } else {
                        Result.failure(e)
                    }
                }
            } else {
                if (cachedProduct != null) {
                    Result.success(cachedProduct.toDomain())
                } else {
                    Result.failure(Exception("No network connection and product not cached"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by ID: $productId", e)
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<List<Product>> {
        return try {
            Log.d(TAG, "Searching products with query: '$query'")

            if (networkUtil.isNetworkAvailable()) {
                try {
                    val response = apiService.searchProducts(query, limit, skip)
                    val products = response.products.map { it.toDomain() }

                    Log.d(TAG, "Search returned ${products.size} products from network")
                    Result.success(products)
                } catch (e: Exception) {
                    Log.e(TAG, "Network search failed, searching in cache", e)

                    // Fallback to cached search
                    val cachedResults = productDao.searchProductsSync(query)
                    Log.d(TAG, "Cache search returned ${cachedResults.size} products")
                    Result.success(cachedResults.map { it.toDomain() })
                }
            } else {
                Log.d(TAG, "Offline: searching in cached products")
                val cachedResults = productDao.searchProductsSync(query)
                Log.d(TAG, "Cache search returned ${cachedResults.size} products")
                Result.success(cachedResults.map { it.toDomain() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products", e)
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            Log.d(TAG, "Getting categories")

            // First, try to get cached categories
            val cachedCategories = categoryDao.getAllCategoriesSync()
            Log.d(TAG, "Found ${cachedCategories.size} cached categories")

            // Check if we should fetch from network
            val shouldFetchFromNetwork = networkUtil.isNetworkAvailable() &&
                    (cachedCategories.isEmpty() || isCategoryCacheStale())

            if (shouldFetchFromNetwork) {
                Log.d(TAG, "Fetching categories from network")

                try {
                    val response = apiService.getCategories()
                    val categories = response.map { it.toDomain() }

                    // Cache the categories
                    Log.d(TAG, "Caching ${categories.size} categories to local database")
                    categoryDao.replaceAllCategories(categories.map { it.toEntity() })

                    Log.d(TAG, "Successfully fetched and cached ${categories.size} categories")
                    Result.success(categories)
                } catch (e: Exception) {
                    Log.e(TAG, "Network request failed for categories, using cached data", e)

                    if (cachedCategories.isNotEmpty()) {
                        Log.d(TAG, "Using ${cachedCategories.size} cached categories as fallback")
                        Result.success(cachedCategories.map { it.toDomain() })
                    } else {
                        Result.failure(e)
                    }
                }
            } else {
                Log.d(TAG, "Using cached categories (${cachedCategories.size} items)")
                Result.success(cachedCategories.map { it.toDomain() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting categories", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductsByCategory(category: String, limit: Int, skip: Int): Result<List<Product>> {
        return try {
            Log.d(TAG, "Getting products by category: $category")

            if (networkUtil.isNetworkAvailable()) {
                try {
                    val response = apiService.getProductsByCategory(category, limit, skip)
                    val products = response.products.map { it.toDomain() }

                    // Update cache with these products
                    products.forEach { product ->
                        productDao.insertProduct(product.toEntity())
                    }

                    Log.d(TAG, "Successfully fetched ${products.size} products for category: $category")
                    Result.success(products)
                } catch (e: Exception) {
                    Log.e(TAG, "Network request failed for category $category, using cached data", e)

                    // Fallback to cached products
                    val cachedProducts = productDao.getProductsByCategorySync(category)
                    Log.d(TAG, "Using ${cachedProducts.size} cached products for category: $category")
                    Result.success(cachedProducts.map { it.toDomain() })
                }
            } else {
                Log.d(TAG, "Offline: using cached products for category: $category")
                val cachedProducts = productDao.getProductsByCategorySync(category)
                Log.d(TAG, "Found ${cachedProducts.size} cached products for category: $category")
                Result.success(cachedProducts.map { it.toDomain() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting products by category: $category", e)
            Result.failure(e)
        }
    }

    // Flow-based methods for real-time data
    fun getProductsFlow(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getProductsByCategoryFlow(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchProductsFlow(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private suspend fun isCacheStale(): Boolean {
        val lastUpdateTime = productDao.getLastUpdateTime() ?: return true
        val currentTime = System.currentTimeMillis()
        val isStale = (currentTime - lastUpdateTime) > CACHE_DURATION_MS
        Log.d(TAG, "Cache stale check: last update was ${currentTime - lastUpdateTime}ms ago, stale: $isStale")
        return isStale
    }

    private suspend fun isCategoryCacheStale(): Boolean {
        val lastUpdateTime = categoryDao.getLastUpdateTime() ?: return true
        val currentTime = System.currentTimeMillis()
        val isStale = (currentTime - lastUpdateTime) > CACHE_DURATION_MS
        Log.d(TAG, "Category cache stale check: last update was ${currentTime - lastUpdateTime}ms ago, stale: $isStale")
        return isStale
    }

    suspend fun clearCache() {
        Log.d(TAG, "Clearing product cache")
        productDao.deleteAllProducts()
        categoryDao.deleteAllCategories()
    }

    suspend fun getCacheInfo(): String {
        val productCount = productDao.getProductCount()
        val categoryCount = categoryDao.getCategoryCount()
        val lastProductUpdate = productDao.getLastUpdateTime()
        val lastCategoryUpdate = categoryDao.getLastUpdateTime()

        return "Products: $productCount, Categories: $categoryCount, " +
                "Last product update: $lastProductUpdate, Last category update: $lastCategoryUpdate"
    }
}

// Extension functions to convert DTO to Domain
private fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand ?: "",
        thumbnail = thumbnail,
        images = images,
        sku = sku,
        weight = weight,
        tags = tags
    )
}

private fun CategoryDto.toDomain(): Category {
    return Category(
        slug = slug,
        name = name,
        url = url
    )
}