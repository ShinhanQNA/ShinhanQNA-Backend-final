package back.sw.domain.post.entity;

public enum BoardType {
    FREE,
    PROJECT_RECRUIT,
    STUDY_RECRUIT,
    QNA;

    public boolean isRecruitBoard() {
        return this == PROJECT_RECRUIT || this == STUDY_RECRUIT;
    }
}
