package ck4.nvb.rsmanagement.core.module.stores.product.service;

import ck4.nvb.rsmanagement.core.module.stores.category.domain.Category;
import ck4.nvb.rsmanagement.core.module.stores.category.domain.CategoryRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.domain.ProductRepository;
import ck4.nvb.rsmanagement.core.module.stores.product.service.dto.ProductCreateDto;
import ck4.nvb.rsmanagement.core.module.users.user.service.dto.UserGetDto;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("productAutoGenerator")
@ConditionalOnProperty(
    name = "product.generator.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class ProductAutoGenerator {

  private final IProductService productService;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Value("${product.generator.max-per-run:5}")
  private int maxPerRun;

  @Value("${product.generator.sku-base:10000}")
  private int skuBase;

  private static final int CREATE_RETRY = 5;

  public ProductAutoGenerator(
      IProductService productService,
      ProductRepository productRepository,
      CategoryRepository categoryRepository) {
    this.productService = productService;
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
  }

  private static final Map<String, CategorySpec> PRODUCT_CATEGORIES_REALISTIC =
      new LinkedHashMap<>();

  static {
    PRODUCT_CATEGORIES_REALISTIC.put(
        "Thực phẩm tươi sống",
        new CategorySpec(
            Arrays.asList(
                "Thịt bò Úc",
                "Cá chép giòn",
                "Đùi gà",
                "Cánh gà",
                "Ức gà",
                "Thịt heo sạch",
                "Cá hồi Na Uy",
                "Tôm sú",
                "Rau xanh hữu cơ",
                "Trái cây nhập khẩu"),
            new int[] {50000, 500000}));
    PRODUCT_CATEGORIES_REALISTIC.put(
        "Đồ uống",
        new CategorySpec(
            Arrays.asList(
                "Coca Cola",
                "Pepsi",
                "Sting",
                "Nutibut",
                "Trà tea +",
                "Trà xanh C2",
                "Nước suối Lavie",
                "Bia Heineken",
                "Bia Tiger"),
            new int[] {15000, 80000}));
    PRODUCT_CATEGORIES_REALISTIC.put(
        "Snack & Bánh kẹo",
        new CategorySpec(
            Arrays.asList(
                "Bánh Oreo",
                "Kẹo Mentos",
                "Snack Ostar",
                "Bánh Chocopie",
                "Kẹo dẻo Haribo",
                "Bánh Nabati",
                "Kẹo dẻo",
                "Bánh quy Cosy"),
            new int[] {8000, 45000}));
    PRODUCT_CATEGORIES_REALISTIC.put(
        "Gia vị & Gia dụng",
        new CategorySpec(
            Arrays.asList(
                "Nước mắm Nam Ngư",
                "Dầu ăn Tường An",
                "Muối I-ốt",
                "Đường trắng",
                "Bột ngọt Aji",
                "Nước rửa chén Sunlight"),
            new int[] {12000, 120000}));
    PRODUCT_CATEGORIES_REALISTIC.put(
        "Chăm sóc cá nhân",
        new CategorySpec(
            Arrays.asList(
                "Kem đánh răng P/S",
                "Dầu gội Head & Shoulders",
                "Sữa tắm Lifebuoy",
                "Kem dưỡng da Pond's",
                "Nước hoa Lancôme"),
            new int[] {25000, 1500000}));
    PRODUCT_CATEGORIES_REALISTIC.put(
        "Điện tử & Phụ kiện",
        new CategorySpec(
            Arrays.asList(
                "Tai nghe AirPods",
                "Cáp sạc iPhone",
                "Pin Energizer",
                "Sạc dự phòng Xiaomi",
                "Ốp lưng điện thoại"),
            new int[] {50000, 5000000}));
  }

  private static class CategorySpec {
    final List<String> products;
    final int[] priceRange; // [min, max]

    CategorySpec(List<String> products, int[] priceRange) {
      this.products = products;
      this.priceRange = priceRange;
    }
  }

  // scheduled generator
  @Scheduled(fixedRateString = "${product.generator.interval}")
  public void scheduledGenerateRealtime() {
    try {
      // ensure categories present in DB (create missing ones)
      ensureCategoriesExist();

      int toGenerate = ThreadLocalRandom.current().nextInt(1, Math.max(1, maxPerRun) + 1);
      for (int i = 0; i < toGenerate; i++) {
        generateOneProductRealtime();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Create missing categories from PRODUCT_CATEGORIES_REALISTIC into DB. Uses simple name match
   * (trimmed). Transactional to persist categories.
   */
  @Transactional
  protected void ensureCategoriesExist() {
    List<Category> dbCats = categoryRepository.findAll();
    Map<String, Category> existing = new HashMap<>();
    if (dbCats != null) {
      for (Category c : dbCats) {
        if (c != null && c.getName() != null) existing.put(c.getName().trim(), c);
      }
    }

    for (String catName : PRODUCT_CATEGORIES_REALISTIC.keySet()) {
      if (!existing.containsKey(catName)) {
        try {
          Category cat = new Category();
          cat.setName(catName);
          cat.setDescription("Danh mục " + catName);
          Category saved = categoryRepository.save(cat);
          existing.put(saved.getName(), saved);
          System.out.println("Created category: " + saved.getName() + " id=" + saved.getId());
        } catch (Exception e) {
          System.err.println("Failed to create category '" + catName + "': " + e.getMessage());
        }
      }
    }
  }

  @Transactional
  public void generateOneProductRealtime() {
    try {
      List<Category> dbCats = categoryRepository.findAll();
      if (dbCats == null || dbCats.isEmpty()) {
        System.out.println("No categories found in DB -> skip product generation.");
        return;
      }

      // Prefer categories that match our realistic template
      List<Category> preferred = new ArrayList<>();
      for (Category c : dbCats) {
        if (c == null || c.getName() == null) continue;
        if (PRODUCT_CATEGORIES_REALISTIC.containsKey(c.getName())) preferred.add(c);
      }
      Category chosenCategory =
          !preferred.isEmpty()
              ? preferred.get(ThreadLocalRandom.current().nextInt(preferred.size()))
              : dbCats.get(ThreadLocalRandom.current().nextInt(dbCats.size()));

      String chosenCatName = chosenCategory.getName();
      CategorySpec spec = PRODUCT_CATEGORIES_REALISTIC.get(chosenCatName);

      List<String> namePool;
      int[] priceRange;
      if (spec != null) {
        namePool = spec.products;
        priceRange = spec.priceRange;
      } else {
        namePool = Arrays.asList("Sản phẩm A", "Sản phẩm B", "Sản phẩm C");
        priceRange = new int[] {10000, 50000};
      }

      String baseName = namePool.get(ThreadLocalRandom.current().nextInt(namePool.size()));

      // variant (70% single, 30% size variants)
      String variant = "";
      if (ThreadLocalRandom.current().nextDouble() >= 0.7) {
        String[] variants = new String[] {" - Size S", " - Size M", " - Size L"};
        variant = variants[ThreadLocalRandom.current().nextInt(variants.length)];
      }

      int minPrice = Math.max(0, priceRange[0]);
      int maxPrice = Math.max(minPrice + 1, priceRange[1]);
      int unitPrice = ThreadLocalRandom.current().nextInt(minPrice, maxPrice + 1);

      int existingCount = 0;
      try {
        existingCount = productRepository.countProductsByDeletedIsFalse();
      } catch (Exception e) {
        existingCount = 0;
      }

      boolean created = false;
      int attempts = 0;
      while (!created && attempts < CREATE_RETRY) {
        attempts++;
        int offset = ThreadLocalRandom.current().nextInt(1, 10000);
        String sku = "SKU" + (skuBase + existingCount + offset);

        ProductCreateDto dto = new ProductCreateDto();
        dto.setSku(sku);
        dto.setName(baseName + variant);
        dto.setDescription(baseName + " - Chất lượng cao, giá tốt");
        dto.setUnitPrice(unitPrice);
        dto.setCategoryId(chosenCategory.getId());

        UserGetDto systemUser = new UserGetDto();
        systemUser.setId(0L);
        systemUser.setUserName("AUTO_GEN");

        try {
          productService.create(dto, systemUser);
          System.out.println(
              "Auto-created product: name="
                  + dto.getName()
                  + " sku="
                  + dto.getSku()
                  + " category="
                  + chosenCatName);
          created = true;
        } catch (Exception ex) {
          System.err.println(
              "Attempt "
                  + attempts
                  + " failed to create product (sku="
                  + sku
                  + "): "
                  + ex.getMessage());
        }
      }

      if (!created) {
        System.err.println(
            "Giving up creating product " + baseName + " after " + attempts + " attempts.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void generateNNow(int n) {
    int toGen = Math.max(0, n);
    for (int i = 0; i < toGen; i++) generateOneProductRealtime();
  }
}
