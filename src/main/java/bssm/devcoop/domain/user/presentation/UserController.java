package bssm.devcoop.domain.user.presentation;

import bssm.devcoop.domain.user.presentation.dto.UserReadDto;
import bssm.devcoop.domain.book.service.BookService;
import bssm.devcoop.domain.user.service.UserService;
import bssm.devcoop.entity.book.Book;
import bssm.devcoop.entity.book.BookId;
import bssm.devcoop.entity.book.types.BookType;
import bssm.devcoop.entity.user.User;
import bssm.devcoop.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/user", produces = "application/json; charset=UTF8") // 설정 다시 할 것
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/books/{userId}")
    public ResponseEntity<?> publishedBookList(@PathVariable String userId) {
        log.info("Get Published Book List");

        try {
            User user = userService.getUserById(userId);
            log.info("Found User : {}", userId);

            List<Book> bookList = user.getBookList();

            log.info("Mapping BookList to UserReadDto");
            List<UserReadDto.Books> readBookList = bookList.stream()
                    .filter(book -> book.getBookType() == BookType.PUBLISHED)
                    .map(book -> {
                        BookId bookId = book.getId();
                        return UserReadDto.Books.builder()
                                .bookId(bookId.getBookId())
                                .userId(bookId.getUserId())
                                .bookTitle(book.getBookTitle())
                                .likedCount(book.getLikedList().size())
                                .commentCount(book.getCommentList().size())
                                .build();
                    })
                    .collect(Collectors.toList());

            UserReadDto.BooksResponse response = UserReadDto.BooksResponse.builder()
                    .bookList(readBookList)
                    .build();

            log.info("Get Published Book List Success");
            return ResponseEntity.ok().body(response);
        } catch (GlobalException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생하였습니다");
        }
    }
}
