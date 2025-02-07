package fr.eni.tp.filmotheque.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import fr.eni.tp.filmotheque.bo.Film;
import fr.eni.tp.filmotheque.bo.Genre;
import fr.eni.tp.filmotheque.bo.Participant;

@Repository
public class FilmDAOImpl implements FilmDAO {

	private final String FIND_BY_ID = "SELECT ID, TITRE, ANNEE, DUREE, SYNOPSIS, ID_REALISATEUR, ID_GENRE FROM FILM "
			+ " WHERE id = :id";
	private final String FIND_ALL = "SELECT ID, TITRE, ANNEE, DUREE, SYNOPSIS, ID_REALISATEUR, ID_GENRE FROM FILM";
	private final String INSERT = "INSERT INTO FILM(TITRE, ANNEE, DUREE, SYNOPSIS, ID_REALISATEUR, ID_GENRE) "
			+ " VALUES (:titre, :annee, :duree, :synopsis, :idRealisateur, :idGenre)";
	private final String FIND_TITRE = "SELECT TITRE FROM FILM WHERE  id = :id";
	private final String FIND_UNIQUE_TITRE = "SELECT count(titre) FROM FILM WHERE  titre like :titre";

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Override
	public void create(Film film) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue("titre", film.getTitre());
		mapSqlParameterSource.addValue("annee", film.getAnnee());
		mapSqlParameterSource.addValue("duree", film.getDuree());
		mapSqlParameterSource.addValue("synopsis", film.getSynopsis());
		mapSqlParameterSource.addValue("idRealisateur", film.getRealisateur().getId());
		mapSqlParameterSource.addValue("idGenre", film.getGenre().getId());
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(INSERT, mapSqlParameterSource, keyHolder);
		
		if(keyHolder != null && keyHolder.getKey() != null) {
			// Mise à jour de l'identifiant du film auto-généré par la base
			film.setId(keyHolder.getKey().longValue());
		}
		
	}

	@Override
	public Film read(long id) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue("id", id);
		return jdbcTemplate.queryForObject(FIND_BY_ID, mapSqlParameterSource, new FilmRowMapper());
	}

	@Override
	public List<Film> findAll() {
		return jdbcTemplate.query(FIND_ALL, new FilmRowMapper());
	}

	@Override
	public String findTitre(long id) {
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue("id", id);
		return jdbcTemplate.queryForObject(FIND_TITRE, mapSqlParameterSource, String.class);
	}
	
	class FilmRowMapper implements RowMapper<Film>{

		@Override
		public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
			Film f = new Film();
			f.setId(rs.getLong("ID"));
			f.setTitre(rs.getString("TITRE"));
			f.setAnnee(rs.getInt("ANNEE"));
			f.setDuree(rs.getInt("DUREE"));
			f.setSynopsis(rs.getString("SYNOPSIS"));

			// Association pour le réalisateur
			Participant realisateur = new Participant();
			realisateur.setId(rs.getLong("ID_REALISATEUR"));
			f.setRealisateur(realisateur);

			// Association pour le genre
			Genre genre = new Genre();
			genre.setId(rs.getLong("ID_GENRE"));
			f.setGenre(genre);

			return f;
		}
		
	}

	@Override
	public int countTitre(String titre) {
		MapSqlParameterSource map = new MapSqlParameterSource();
		map.addValue("titre", titre);
		return jdbcTemplate.queryForObject(FIND_UNIQUE_TITRE, map, Integer.class);
	}

}
