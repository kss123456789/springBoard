package com.sparta.board.service;

import com.sparta.board.dto.BoardRequestDto;
import com.sparta.board.dto.BoardResponseDto;
import com.sparta.board.dto.StatusCodesResponseDto;
import com.sparta.board.entity.Board;
import com.sparta.board.jwt.JwtUtil;
import com.sparta.board.repository.BoardRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final JwtUtil jwtUtil;

    public BoardResponseDto createBoard(BoardRequestDto requestDto, String token) {
        // 토큰으로 부터 유저이름 가져오기
        String username = getUsername(token);

        Board board = new Board(requestDto, username);
        // DB 저장 넘겨주기
        Board saveBoard = boardRepository.save(board);

        // Entity -> ResponseDto
        BoardResponseDto boardResponseDto = new BoardResponseDto(saveBoard);

        return boardResponseDto;
    }
    public List<BoardResponseDto> getBoards() {
        // db 조회 넘겨주기
        return boardRepository.findAllByOrderByCreatedAtDesc().stream().map(BoardResponseDto::new).toList();
    }

    public BoardResponseDto getSelectedBoard(Long id) {
        // 해당 메모가 DB에 존재하는지 확인
        Board board = findBoard(id);
        // Entity -> ResponseDto
        BoardResponseDto boardResponseDto = new BoardResponseDto(board);
        return boardResponseDto;
    }

    @Transactional
    public BoardResponseDto updateBoard(Long id, BoardRequestDto requestDto, String token) {
        // 해당 메모가 DB에 존재하는지 확인
        Board board = findBoard(id);
        // username 확인
        checkUsername(board, token);
        // 수정
        board.update(requestDto);
        // Entity -> ResponseDto
        BoardResponseDto boardResponseDto = new BoardResponseDto(board);
        return boardResponseDto;
    }

    public StatusCodesResponseDto deleteBoard(Long id, String token) {
        // 해당 메모가 DB에 존재하는지 확인
        Board board = findBoard(id);
        // username 확인
        checkUsername(board, token);
        // 삭제
        boardRepository.delete(board);

        StatusCodesResponseDto responseDto = new StatusCodesResponseDto(HttpStatus.OK.value(), "삭제가 완료 되었습니다.");

        return responseDto;

    }

    private Board findBoard(Long id) {
        return boardRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 게시물 입니다.")
        );
    }

    private String getUsername(String token) {
        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);
        // 사용자 username
        String username = info.getSubject();

        return username;
    }

    private void checkUsername(Board board, String token) {
        String username = getUsername(token);

        if (!board.getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자가 아닙니다.");
        }
    }
}
