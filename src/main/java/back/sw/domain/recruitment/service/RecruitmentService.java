package back.sw.domain.recruitment.service;

import back.sw.domain.post.entity.Post;
import back.sw.domain.recruitment.dto.request.RecruitmentCreateRequest;
import back.sw.domain.recruitment.dto.response.RecruitmentDetailResponse;
import back.sw.domain.recruitment.entity.RecruitStatus;
import back.sw.domain.recruitment.entity.RecruitmentDetail;
import back.sw.domain.recruitment.repository.RecruitmentDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {
    private final RecruitmentDetailRepository recruitmentDetailRepository;

    @Transactional
    public void createForPost(Post post, RecruitmentCreateRequest request) {
        RecruitmentDetail detail = RecruitmentDetail.createOpen(
                post,
                request.capacity(),
                request.contactMethod(),
                request.deadline()
        );
        recruitmentDetailRepository.save(detail);
    }

    public Optional<RecruitmentDetailResponse> getDetailResponseByPostId(int postId) {
        return recruitmentDetailRepository.findByPostId(postId)
                .map(detail -> new RecruitmentDetailResponse(
                        detail.getCapacity(),
                        detail.getCurrentCount(),
                        detail.getContactMethod(),
                        detail.getRecruitStatus(),
                        detail.getDeadline()
                ));
    }

    @Transactional
    public int closeExpiredOpenRecruitments(LocalDate today) {
        return recruitmentDetailRepository.closeExpiredOpenRecruitments(
                today,
                RecruitStatus.OPEN,
                RecruitStatus.CLOSED
        );
    }
}
