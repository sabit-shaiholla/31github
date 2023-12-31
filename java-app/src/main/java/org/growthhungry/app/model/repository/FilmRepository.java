package org.growthhungry.app.model.repository;

import com.speedment.jpastreamer.application.JPAStreamer;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.streamconfiguration.StreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.growthhungry.app.model.Film;
import org.growthhungry.app.model.Film$;
import org.growthhungry.app.model.FilmRatingEntity;
import org.growthhungry.app.model.FilmRatingEntity$;

import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class FilmRepository {

    @Inject
    JPAStreamer jpaStreamer;

    private static final int PAGE_SIZE = 20;

    public Optional<Film> getFilm(short filmId) {
        return jpaStreamer.stream(Film.class)
                .filter(Film$.filmId.equal(filmId))
                .findFirst();
    }

    public Stream<Film> getFilms(short minLength) {
        return jpaStreamer.stream(Film.class)
                .filter(Film$.length.greaterOrEqual(minLength))
                .sorted(Film$.length);
    }

    public Stream<Film> paged(long page, short minLength) {
        return jpaStreamer.stream(Projection.select(Film$.filmId, Film$.title, Film$.length))
                .filter(Film$.length.greaterOrEqual(minLength))
                .sorted(Film$.length)
                .skip(page * PAGE_SIZE)
                .limit(PAGE_SIZE);
    }

    public Stream<Film> actors(String startsWith, short minLength) {
        final StreamConfiguration<Film> sc =
                StreamConfiguration.of(Film.class).joining(Film$.actors);
        return jpaStreamer.stream(sc)
                .filter(Film$.title.startsWith(startsWith).and(Film$.length.greaterOrEqual(minLength)))
                .sorted(Film$.length.reversed());
    }

    @Transactional
    public void updateRentalRate(short minLength, Float rentalRate) {
        jpaStreamer.stream(Film.class)
                .filter(Film$.length.greaterOrEqual(minLength))
                .forEach(f -> {
                    f.setRentalRate(rentalRate);
                });
    }

    public Stream<FilmRatingEntity> getFilmRating(short filmId) {
        return jpaStreamer.stream(FilmRatingEntity.class)
                .filter(FilmRatingEntity$.filmId.equal(filmId))
                .sorted(FilmRatingEntity$.stars);
    }

//    @Transactional
//    public void addFilmRating(short filmId, short stars) {
//        jpaStreamer.stream(FilmRatingEntity.class)
//                .filter(FilmRatingEntity$.filmId.equal(filmId))
//                .filter(FilmRatingEntity$.stars.equal(stars))
//                .findFirst()
//                .ifPresentOrElse(
//                        f -> {
//                            f.setStars(stars);
//                        },
//                        () -> {
//                            jpaStreamer.persist(new FilmRatingEntity(filmId, stars));
//                        }
//                );
//    }
}
