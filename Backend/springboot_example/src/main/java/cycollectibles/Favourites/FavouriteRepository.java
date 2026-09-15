package cycollectibles.Favourites;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FavouriteRepository extends JpaRepository<Favourite, Integer> {
    List<Favourite> findByUser_Id(Integer userId);
    boolean existsByUser_IdAndCategory(Integer userId, String category);
    @Transactional
    void deleteByUser_IdAndCategory(Integer userId, String category);
    List<Favourite> findByCategory(String category);
}
