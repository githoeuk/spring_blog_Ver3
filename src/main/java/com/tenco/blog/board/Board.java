package com.tenco.blog.board;

import com.tenco.blog._core.errors.Exception403;
import com.tenco.blog._core.util.MyDateUtil;
import com.tenco.blog.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data // get,set, toString ..
// @Entity - JPA가 이 클래스를 데이터베이스 테이블과 매핑하는 객체로 인식하게 설정
// 즉, 이 어노테이션이 있어야 JPA가 관리 함
@Entity
@Table(name = "board_tb")
@NoArgsConstructor
@AllArgsConstructor // 전체 멤벼 번수를 넣을 수 있는 생성자.
@Builder
public class Board {

    // @id : 이 필드가 기본키임을 설정 함
    @Id
    // IDENTITY 전략: 데이터베이스게 기본 AUTO_INCREMENT 기능 사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //    private String username;  삭제해야 함.
    private String title;
    private String content;

    // 신규
    @ColumnDefault("false")
    @Builder.Default // 다른 파일에서 Builder 객체 생성 시 null 값 세팅을 방지
    private Boolean premium = false;


    // 연관관계 설정 해주어야 한다.
    // 다대일 연관관계 : 여러개 게시글이 하나의 사용자에게 속한다.
    // FetchType 전략 : EAGER, LAZY
    //   EAGER - 조회시 한번에 다 들고 와라 ( 1번 게시글 조회시 한번 조인까지 해라)
    //   LAZY - 처음부터 Board 조회할 때 User 정보를 가져오지 마. 필요할 때 한번 더 조회 해.
    @ManyToOne(fetch = FetchType.LAZY)
    // @OneToMany
    // @OneToOne
    @JoinColumn(name = "user_id") // 외래키 컬럼명 표시 됨
    private User user;

    // @CreationTimestamp : 하이버네이트가 제공하는 어노테이션
    // 특정 하나의 엔티티가 저장이 될 때 현재 시간을 자동으로 저장해 설정
    // now() 명시할 필요 없음
    // pc --> db (자동 날짜 주입)
    @CreationTimestamp
    private Timestamp createdAt;

    // createdAt -> 포멧 하는 메서드 만들어 보기
    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }

    // 수정 편의 기능 만들기
    public void update(BoardRequest.UpdateDTO updateDTO) {
        // this.username = updateDTO.getUsername(); 삭제 예정
        this.title = updateDTO.getTitle();
        this.content = updateDTO.getContent();

        // 신규
        // 유료 여부도 함께 업데이트 ( 값없으면 false 있으면 값 변경 )
        this.premium = (updateDTO.getPremium() != null ? updateDTO.getPremium() : false);

    }

    // 편의 기능 - 게시글 소유자 확인을 위한 기능 추가
    public boolean isOwner(Integer sessionUserId) {
        if (!this.user.getId().equals(sessionUserId)) {
            throw new Exception403("본인이 작성한 게시글이 아닙니다");
        }
        return true;
    }

} // end of class

