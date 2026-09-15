package cycollectibles.Admin;

import cycollectibles.Postings.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannedPostingsRepository extends JpaRepository<BannedPostings, Integer> {
    Boolean existsByPosting_PostingId(Integer postingId);
}
