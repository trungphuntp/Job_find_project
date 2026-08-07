package com.project.codinviec_core_service.service.imp;

import com.project.codinviec_core_service.dto.BlogDTO;
import com.project.codinviec_core_service.dto.BlogDetailDTO;
import com.project.codinviec_core_service.entity.Blog;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.BlogMapper;
import com.project.codinviec_core_service.repository.BlogRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.SaveUpdateBlogRequest;
import com.project.codinviec_core_service.service.BlogService;
import com.project.codinviec_core_service.specification.BlogSpecification;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogServiceImp implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final PageCustomHelper pageCustomHelper;
    private final BlogSpecification blogSpecification;

    @Override
    public List<BlogDTO> getAllBlog() {
        return blogRepository.findAll().stream()
                .map(blogMapper::blogToDTO)
                .toList();
    }

    @Override
    public Page<BlogDTO> getAllBlogPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);

        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "createdDateAsc" -> Sort.by(Sort.Direction.ASC, "createdDate");
            case "createdDateDesc" -> Sort.by(Sort.Direction.DESC, "createdDate");
            case "sortHighlight" -> Sort.by(Sort.Direction.DESC, "isHighLight");
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);
        Specification<Blog> spec = Specification
                .allOf(blogSpecification.searchByName(pageRequestValidate.getKeyword()));
        return blogRepository.findAll(spec, pageable)
                .map(blogMapper::blogToDTO);
    }

    @Override
    public BlogDTO getBlogById(Integer id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy blog ID"));
        return blogMapper.blogToDTO(blog);
    }

    @Override
    @Transactional
    public BlogDTO saveBlog(SaveUpdateBlogRequest saveUpdateBlogRequest) {
        try {
            Blog blog = blogMapper.saveBlogMapper(saveUpdateBlogRequest);
            return blogMapper.blogToDTO(blogRepository.save(blog));
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Lỗi thêm blog!");
        }
    }

    @Override
    @Transactional
    public BlogDTO updateBlogById(Integer idBlog, SaveUpdateBlogRequest saveUpdateBlogRequest) {
        Blog blog = blogRepository.findById(idBlog)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy blog ID"));

        try {
            Blog mappedBlog = blogMapper.updateBlogMapper(idBlog, saveUpdateBlogRequest);
            mappedBlog.setCreatedDate(blog.getCreatedDate());
            return blogMapper.blogToDTO(blogRepository.save(mappedBlog));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Lỗi cập nhật blog!");
        }
    }

    @Override
    @Transactional
    public BlogDTO deleteBlogById(Integer id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy blog ID"));
        blogRepository.delete(blog);
        return blogMapper.blogToDTO(blog);
    }

    @Override
    public BlogDetailDTO getBlogDetailById(Integer id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy blog ID"));
        return blogMapper.blogToBlogDetailDTO(blog);
    }
}
