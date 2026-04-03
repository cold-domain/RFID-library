package com.library;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.constant.Constants;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.controller.*;
import com.library.dto.BookDTO;
import com.library.dto.CategoryDTO;
import com.library.entity.*;
import com.library.service.*;
import com.library.vo.BookVO;
import com.library.vo.BorrowRecordVO;
import com.library.vo.CategoryVO;
import com.library.vo.FineRecordVO;
import com.library.vo.ReservationVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.library.TestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;
    @Mock
    private RfidBindHistoryService rfidBindHistoryService;
    @Mock
    private BookStatusHistoryService bookStatusHistoryService;

    @InjectMocks
    private BookController controller;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void listReturnsPagedBooks() {
        Page<BookVO> page = pageOf(new BookVO());
        when(bookService.getBookPage(1, 10, "java", 2L, "on_shelf")).thenReturn(page);

        Result<?> result = controller.list(1, 10, "java", 2L, "on_shelf");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void getByIdReturnsErrorWhenBookMissing() {
        when(bookService.getBookDetail(1L)).thenReturn(null);

        Result<?> result = controller.getById(1L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void addUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(21L, "librarian"));
        BookDTO dto = new BookDTO();
        dto.setTitle("Book");

        Result<?> result = controller.add(dto);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookService).addBook(dto, 21L);
    }

    @Test
    void updateUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(21L, "librarian"));
        BookDTO dto = new BookDTO();
        dto.setTitle("Book");

        Result<?> result = controller.update(3L, dto);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookService).updateBook(3L, dto, 21L);
    }

    @Test
    void deleteDelegatesToService() {
        Result<?> result = controller.delete(6L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookService).removeById(6L);
    }

    @Test
    void bindRfidUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(21L, "librarian"));

        Result<?> result = controller.bindRfid(5L, "RFID-1");

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookService).bindRfidTag(5L, "RFID-1", 21L);
    }

    @Test
    void unbindRfidUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(21L, "librarian"));

        Result<?> result = controller.unbindRfid(5L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookService).unbindRfidTag(5L, 21L);
    }

    @Test
    void updateStatusUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(21L, "librarian"));

        Result<?> result = controller.updateStatus(5L, "borrowed");

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookService).updateCollectionStatus(5L, "borrowed", 21L);
    }

    @Test
    void rfidHistoryReturnsHistory() {
        List<RfidBindHistory> history = List.of(new RfidBindHistory());
        when(rfidBindHistoryService.getHistoryByBookId(5L)).thenReturn(history);

        Result<?> result = controller.rfidHistory(5L);

        assertThat(result.getData()).isEqualTo(history);
    }

    @Test
    void statusHistoryReturnsHistory() {
        List<BookStatusHistory> history = List.of(new BookStatusHistory());
        when(bookStatusHistoryService.getHistoryByBookId(5L)).thenReturn(history);

        Result<?> result = controller.statusHistory(5L);

        assertThat(result.getData()).isEqualTo(history);
    }
}

@ExtendWith(MockitoExtension.class)
class BookCategoryControllerTest {

    @Mock
    private BookCategoryService bookCategoryService;

    @InjectMocks
    private BookCategoryController controller;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void treeReturnsCategoryTree() {
        List<CategoryVO> tree = List.of(categoryVo(1L, "Root", null));
        when(bookCategoryService.getCategoryTree()).thenReturn(tree);

        Result<List<CategoryVO>> result = controller.tree();

        assertThat(result.getData()).isEqualTo(tree);
    }

    @Test
    void listReturnsEnabledCategories() {
        List<CategoryVO> categories = List.of(categoryVo(1L, "Root", null));
        when(bookCategoryService.getEnabledCategories()).thenReturn(categories);

        Result<List<CategoryVO>> result = controller.list();

        assertThat(result.getData()).isEqualTo(categories);
    }

    @Test
    void addUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(31L, "librarian"));
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Technology");

        Result<?> result = controller.add(dto);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookCategoryService).addCategory(dto, 31L);
    }

    @Test
    void updateDelegatesToService() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Updated");

        Result<?> result = controller.update(4L, dto);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookCategoryService).updateCategory(4L, dto);
    }

    @Test
    void deleteDelegatesToService() {
        Result<?> result = controller.delete(4L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookCategoryService).removeById(4L);
    }
}

@ExtendWith(MockitoExtension.class)
class BorrowControllerTest {

    @Mock
    private BorrowRecordService borrowRecordService;
    @Mock
    private BookService bookService;

    @InjectMocks
    private BorrowController controller;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void searchBooksUsesBookService() {
        Page<BookVO> page = pageOf(new BookVO());
        when(bookService.getBookPage(1, 10, "java", 2L, "on_shelf")).thenReturn(page);

        Result<?> result = controller.searchBooks(1, 10, "java", 2L, "on_shelf");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void borrowUsesCurrentUser() {
        BorrowRecord record = new BorrowRecord();
        ContextHolder.setCurrentUser(user(40L, "operator"));
        when(borrowRecordService.borrowBook(2L, 3L, 40L)).thenReturn(record);

        Result<?> result = controller.borrow(2L, 3L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        assertThat(result.getData()).isEqualTo(record);
    }

    @Test
    void returnBookUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(40L, "operator"));

        Result<?> result = controller.returnBook(8L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(borrowRecordService).returnBook(8L, 40L);
    }

    @Test
    void renewUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(40L, "operator"));

        Result<?> result = controller.renew(8L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(borrowRecordService).renewBook(8L, 40L);
    }

    @Test
    void listReturnsBorrowPage() {
        Page<BorrowRecordVO> page = pageOf(new BorrowRecordVO());
        when(borrowRecordService.getBorrowRecordPage(1, 10, 2L, "borrowed")).thenReturn(page);

        Result<?> result = controller.list(1, 10, 2L, "borrowed");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void checkOverdueDelegatesToService() {
        Result<?> result = controller.checkOverdue();

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(borrowRecordService).checkOverdue();
    }
}

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private BookService bookService;
    @Mock
    private UserService userService;
    @Mock
    private BorrowRecordService borrowRecordService;
    @Mock
    private BookCategoryService bookCategoryService;

    @InjectMocks
    private DashboardController controller;

    @Test
    void getStatsAggregatesDashboardData() {
        Book javaBook = new Book();
        javaBook.setCategoryId(1L);
        Book springBook = new Book();
        springBook.setCategoryId(1L);
        Book uiBook = new Book();
        uiBook.setCategoryId(2L);

        BookCategory tech = new BookCategory();
        tech.setCategoryId(1L);
        tech.setCategoryName("Technology");
        BookCategory design = new BookCategory();
        design.setCategoryId(2L);
        design.setCategoryName("Design");

        BorrowRecord today = new BorrowRecord();
        today.setBorrowTime(LocalDateTime.now());
        BorrowRecord yesterday = new BorrowRecord();
        yesterday.setBorrowTime(LocalDateTime.now().minusDays(1));

        when(bookService.count()).thenReturn(12L);
        when(userService.count()).thenReturn(5L);
        when(borrowRecordService.count(any())).thenReturn(3L, 1L);
        when(bookService.list()).thenReturn(List.of(javaBook, springBook, uiBook));
        when(bookCategoryService.list()).thenReturn(List.of(tech, design));
        when(borrowRecordService.list(any())).thenReturn(List.of(today, yesterday));

        Result<?> result = controller.getStats();

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        Map<String, Object> data = castMap(result.getData());
        assertThat(data.get("totalBooks")).isEqualTo(12L);
        assertThat(data.get("totalUsers")).isEqualTo(5L);
        assertThat(data.get("activeBorrows")).isEqualTo(3L);
        assertThat(data.get("overdueCount")).isEqualTo(1L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryStats = (List<Map<String, Object>>) data.get("categoryStats");
        assertThat(categoryStats).hasSize(2);
        assertThat(categoryStats.get(0).get("name")).isEqualTo("Technology");
        assertThat(categoryStats.get(0).get("value")).isEqualTo(2L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> borrowTrend = (List<Map<String, Object>>) data.get("borrowTrend");
        assertThat(borrowTrend).hasSize(7);
        assertThat(borrowTrend).anySatisfy(item -> assertThat(item.get("count")).isEqualTo(1L));
    }
}

@ExtendWith(MockitoExtension.class)
class FineControllerTest {

    @Mock
    private FineRecordService fineRecordService;

    @InjectMocks
    private FineController controller;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void listReturnsFinePage() {
        Page<FineRecordVO> page = pageOf(new FineRecordVO());
        when(fineRecordService.getFinePage(1, 10, 2L, "unpaid")).thenReturn(page);

        Result<?> result = controller.list(1, 10, 2L, "unpaid");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void createUsesCurrentUser() {
        FineRecord fine = new FineRecord();
        ContextHolder.setCurrentUser(user(50L, "operator"));
        when(fineRecordService.createFine(2L, 3L, "damage", new BigDecimal("12.50"), 5, 50L)).thenReturn(fine);

        Result<?> result = controller.create(2L, 3L, "damage", new BigDecimal("12.50"), 5);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        assertThat(result.getData()).isEqualTo(fine);
    }

    @Test
    void payUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(50L, "operator"));

        Result<?> result = controller.pay(4L, new BigDecimal("5.00"));

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(fineRecordService).payFine(4L, new BigDecimal("5.00"), 50L);
    }

    @Test
    void waiveUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(50L, "operator"));

        Result<?> result = controller.waive(4L, "manual");

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(fineRecordService).waiveFine(4L, "manual", 50L);
    }

    @Test
    void unpaidAmountReturnsAggregatedAmount() {
        when(fineRecordService.getUnpaidAmount(8L)).thenReturn(new BigDecimal("20.00"));

        Result<?> result = controller.unpaidAmount(8L);

        assertThat(result.getData()).isEqualTo(new BigDecimal("20.00"));
    }
}

@ExtendWith(MockitoExtension.class)
class PublicBookControllerTest {

    @Mock
    private BookService bookService;
    @Mock
    private BookCategoryService bookCategoryService;

    @InjectMocks
    private PublicBookController controller;

    @Test
    void listReturnsPublicBooks() {
        Page<BookVO> page = pageOf(new BookVO());
        when(bookService.getBookPage(1, 10, "java", 2L, null)).thenReturn(page);

        Result<?> result = controller.list(1, 10, "java", 2L);

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void getByIdReturnsErrorWhenBookMissing() {
        when(bookService.getBookDetail(2L)).thenReturn(null);

        Result<?> result = controller.getById(2L);

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void categoriesReturnsCategoryTree() {
        List<CategoryVO> categories = List.of(categoryVo(1L, "Technology", null));
        when(bookCategoryService.getCategoryTree()).thenReturn(categories);

        Result<?> result = controller.categories();

        assertThat(result.getData()).isEqualTo(categories);
    }
}

@ExtendWith(MockitoExtension.class)
class ReaderControllerTest {

    @Mock
    private BorrowRecordService borrowRecordService;
    @Mock
    private BookReservationService bookReservationService;
    @Mock
    private FineRecordService fineRecordService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ReaderController controller;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void getProfileReturnsErrorWhenUserMissing() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        when(userService.getById(60L)).thenReturn(null);

        Result<?> result = controller.getProfile();

        assertThat(result.getCode()).isEqualTo(Constants.ERROR);
    }

    @Test
    void getProfileClearsPassword() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        User profile = user(60L, "reader");
        profile.setPassword("secret");
        when(userService.getById(60L)).thenReturn(profile);

        Result<?> result = controller.getProfile();

        User returned = (User) result.getData();
        assertThat(returned.getPassword()).isNull();
    }

    @Test
    void updateProfileSanitizesForbiddenFields() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        User payload = user(null, "reader");
        payload.setPassword("secret");
        payload.setAccountStatus(0);
        payload.setMaxBorrowCount(99);

        Result<?> result = controller.updateProfile(payload);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userService).updateById(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(60L);
        assertThat(saved.getPassword()).isNull();
        assertThat(saved.getAccountStatus()).isNull();
        assertThat(saved.getMaxBorrowCount()).isNull();
    }

    @Test
    void changePasswordUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(60L, "reader"));

        Result<?> result = controller.changePassword(Map.of("oldPassword", "old", "newPassword", "new"));

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(userService).changePassword(60L, "old", "new");
    }

    @Test
    void myBorrowsReturnsCurrentUserBorrows() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        Page<BorrowRecordVO> page = pageOf(new BorrowRecordVO());
        when(borrowRecordService.getBorrowRecordPage(1, 10, 60L, "borrowed")).thenReturn(page);

        Result<?> result = controller.myBorrows(1, 10, "borrowed");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void renewUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(60L, "reader"));

        Result<?> result = controller.renew(11L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(borrowRecordService).renewBook(11L, 60L);
    }

    @Test
    void reserveUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        BookReservation reservation = new BookReservation();
        when(bookReservationService.reserveBook(60L, 9L)).thenReturn(reservation);

        Result<?> result = controller.reserve(9L);

        assertThat(result.getData()).isEqualTo(reservation);
    }

    @Test
    void myReservationsMapsCompletedStatus() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        Page<ReservationVO> page = pageOf(new ReservationVO());
        when(bookReservationService.getReservationPage(1, 10, 60L, "picked_up")).thenReturn(page);

        Result<?> result = controller.myReservations(1, 10, "completed");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void cancelReservationUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(60L, "reader"));

        Result<?> result = controller.cancelReservation(3L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookReservationService).cancelReservation(3L, 60L);
    }

    @Test
    void myFinesReturnsCurrentUserFines() {
        ContextHolder.setCurrentUser(user(60L, "reader"));
        Page<FineRecordVO> page = pageOf(new FineRecordVO());
        when(fineRecordService.getFinePage(1, 10, 60L, "unpaid")).thenReturn(page);

        Result<?> result = controller.myFines(1, 10, "unpaid");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void payFineUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(60L, "reader"));

        Result<?> result = controller.payFine(5L, new BigDecimal("8.00"));

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(fineRecordService).payFine(5L, new BigDecimal("8.00"), 60L);
    }
}

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private BookReservationService bookReservationService;

    @InjectMocks
    private ReservationController controller;

    @AfterEach
    void tearDown() {
        ContextHolder.clear();
    }

    @Test
    void listMapsCompletedStatusToPickedUp() {
        Page<ReservationVO> page = pageOf(new ReservationVO());
        when(bookReservationService.getReservationPage(1, 10, 4L, "picked_up")).thenReturn(page);

        Result<?> result = controller.list(1, 10, 4L, "completed");

        assertThat(result.getData()).isEqualTo(page);
    }

    @Test
    void confirmPickupUsesCurrentUser() {
        ContextHolder.setCurrentUser(user(70L, "operator"));

        Result<?> result = controller.confirmPickup(6L);

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookReservationService).confirmPickup(6L, 70L);
    }

    @Test
    void checkExpiredDelegatesToService() {
        Result<?> result = controller.checkExpired();

        assertThat(result.getCode()).isEqualTo(Constants.SUCCESS);
        verify(bookReservationService).checkExpiredReservations();
    }
}
