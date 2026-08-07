package com.project.codinviec_core_service.service.imp;

import com.project.codinviec_core_service.dto.CategoryDTO;
import com.project.codinviec_core_service.entity.Category;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.CategoryMapper;
import com.project.codinviec_core_service.repository.CategoryRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.SaveUpdateCategoryRequest;
import com.project.codinviec_core_service.service.CategoryService;
import com.project.codinviec_core_service.specification.CategorySpecification;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final PageCustomHelper pageCustomHelper;
    private final CategorySpecification categorySpecification;

    private CategoryDTO buildTree(Category category, Map<Integer, List<Category>> childrenMap) {
        CategoryDTO dto = categoryMapper.categoryToDTONoChildren(category);
        dto.setChildren(
                childrenMap.getOrDefault(category.getId(), List.of()).stream()
                        .map(child -> buildTree(child, childrenMap))
                        .toList()
        );
        return dto;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> all = categoryRepository.findAllCategory();

        Map<Integer, List<Category>> childrenMap = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return all.stream()
                .filter(c -> c.getParent() == null)
                .map(root -> buildTree(root, childrenMap))
                .toList();
    }

    @Override
    public Page<CategoryDTO> getAllCategoriesPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());
        Specification<Category> spec = Specification.allOf(
                categorySpecification.searchByName(pageRequestValidate.getKeyword()),
                categorySpecification.parentIsNull());
        return categoryRepository.findAll(spec, pageable)
                .map(categoryMapper::categoryToDTONoChildren);
    }

    @Override
    public CategoryDTO getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id category"));
        return categoryMapper.categoryToCategoryDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO saveCategory(SaveUpdateCategoryRequest saveUpdateCategoryRequest) {
        Category categoryParent = null;
        if (saveUpdateCategoryRequest.getParentId() != null && saveUpdateCategoryRequest.getParentId() > 0) {
            categoryParent = categoryRepository.findById(saveUpdateCategoryRequest.getParentId()).orElseThrow(
                    () -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id cha của category!"));
        }
        try {
            Category mappedCategory = categoryMapper.saveCategoryMapper(categoryParent, saveUpdateCategoryRequest);
            return categoryMapper.categoryToCategoryDTO(categoryRepository.save(mappedCategory));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Lỗi thêm category");
        }
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Integer idCate, SaveUpdateCategoryRequest saveUpdateCategoryRequest) {
        Category categoryParent = null;
        if (saveUpdateCategoryRequest.getParentId() != null && saveUpdateCategoryRequest.getParentId() > 0) {
            categoryParent = categoryRepository.findById(saveUpdateCategoryRequest.getParentId()).orElseThrow(
                    () -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id cha của category!"));
        }
        Category category = categoryRepository.findById(idCate)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id category"));
        try {
            Category mapperBlog = categoryMapper.updateCategoryMapper(idCate, categoryParent, saveUpdateCategoryRequest);
            mapperBlog.setCreatedDate(category.getCreatedDate());
            return categoryMapper.categoryToCategoryDTO(categoryRepository.save(mapperBlog));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Lỗi cập nhật category!");
        }
    }

    @Override
    @Transactional
    public CategoryDTO deleteCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id category"));
        categoryRepository.delete(category);
        return categoryMapper.categoryToCategoryDTO(category);
    }
}
