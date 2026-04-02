package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.BookDTO;
import com.library.entity.Book;
import com.library.vo.BookVO;

/**
 * 图书服务接口
 */
public interface BookService extends IService<Book> {

    /**
     * 分页查询图书列表
     */
    IPage<BookVO> getBookPage(int pageNum, int pageSize, String keyword, Long categoryId, String status);

    /**
     * 查询图书详情（返回VO）
     */
    BookVO getBookDetail(Long id);

    /**
     * 新增图书
     */
    void addBook(BookDTO dto, Long creatorId);

    /**
     * 修改图书
     */
    void updateBook(Long id, BookDTO dto, Long updaterId);

    /**
     * 根据ISBN查询图书
     */
    Book getByIsbn(String isbn);

    /**
     * 根据RFID标签查询图书
     */
    Book getByRfidTag(String rfidTag);

    /**
     * 绑定RFID标签
     */
    void bindRfidTag(Long bookId, String rfidTag, Long operatorId);

    /**
     * 解绑RFID标签
     */
    void unbindRfidTag(Long bookId, Long operatorId);

    /**
     * 更新图书馆藏状态
     */
    void updateCollectionStatus(Long bookId, String status, Long operatorId);
}
