package com.VMcom.VMcom.services;


import com.VMcom.VMcom.model.Product;
import com.VMcom.VMcom.model.ProductCategory;
import com.VMcom.VMcom.repository.ProductCategoryRepository;
import com.VMcom.VMcom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service

public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private final Path rootLocation = Paths.get("src/main/resources/static/uploaded-pictures");

    public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;


        // verify if storage can be initialized
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }

    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public List<ProductCategory> getAllProductCategories() {return  productCategoryRepository.findAll();}





    public ProductCategory addProductCategory(ProductCategory productCategory){

        //TODO ADD verification if category already exist in database.
        ProductCategory newProductCategory = new ProductCategory(productCategory.getName());

        ProductCategory productCategory1 = productCategoryRepository.save(newProductCategory);

        return productCategory1;

    }


    public ProductCategory updateProductCategory(Long categoryId, String name){

      ProductCategory productCategory =  productCategoryRepository.findById(categoryId).orElseThrow(()-> new IllegalStateException("Product category with id:"+categoryId+"does not exist in database"));

      productCategory.setName(name);

      productCategoryRepository.save(productCategory);

        System.out.println(productCategory.getName());

      return productCategory;

    }


    public boolean addProduct(Product product, List<MultipartFile> pictureFiles ){


        List<String> photosURL = new ArrayList<>();


        //Adding photo from list to photo storage
        pictureFiles.forEach( pictureFile ->{


            if (!pictureFile.isEmpty()) {

                try {
                    // This is where we will save the file
                    Path destinationFile = rootLocation.resolve(
                            Paths.get(product.getName() + "_" + pictureFile.getOriginalFilename())).normalize().toAbsolutePath();

                    Files.copy(pictureFile.getInputStream(), destinationFile);

                    photosURL.add("/api/v1/product/images/"+product.getName()+"_"+pictureFile.getOriginalFilename());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("File upload failed: " + e.getMessage());
                }

            }else {

            }



        });



        Product newProduct = new Product(product.getName(),product.getDescription(),product.getPrice(),photosURL,product.getAmount(),product.getProductCategory());

        productRepository.save(newProduct);

        return true;
    }

    public List<Product> getProductsByCategory(Long categoryId){

        return productRepository.findByCategory(categoryId);
    }


    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(()-> new IllegalStateException("Product with id:"+productId+"does not exist in database"));
    }




}
