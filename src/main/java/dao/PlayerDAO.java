package dao;

import lombok.RequiredArgsConstructor;
import model.Player;
import Exception.PlayerRegistrationFailureException;
import Exception.PlayerUpdateFailureException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@RequiredArgsConstructor
public class PlayerDAO {

    private final Connection connection;

    /**
     * Register Player
     *
     * @param name 선수 이름
     * @param position 포지션
     * @param teamId 팀 아이디
     * @return inserted Player id
     * @throws SQLException
     */
    public Long registerPlayer(Long teamId, String name, String position) throws SQLException {

        String query = "INSERT INTO player (team_id, name, position) VALUES (?, ?, ?)";

        long playerId;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query, RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, teamId);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, position);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows < 0) {
                throw new PlayerRegistrationFailureException("Failed to Register player while execute SQL");
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()){
                playerId = getAutoGeneratedId(generatedKeys);
            }
        }
        return playerId;
    }

    /**
     * get Auto-generated Key of insert query
     *
     * @param resultSet of getGeneratedKeys()
     * @return Auto-generated Key
     * @throws SQLException
     */
    private Long getAutoGeneratedId(ResultSet resultSet) throws SQLException {

        if (resultSet.next()) {
            return resultSet.getLong(1);
        }
        throw new SQLException("Can not Find autogenerated ID");
    }

    public void updatePlayer(Long id) throws SQLException {

        String query = "UPDATE player SET team_id = -1 WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query, RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, id);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows < 0) {
                throw new PlayerUpdateFailureException("Failed to Update player while execute SQL");
            }

        }
    }

    /**
     * Find Player by player id
     *
     * @param id 선수 id
     * @return Matched Player by id as Optional, Optional.empty() if not found
     */
    public Optional<Player> findById(Long id) throws SQLException {

        String query = "SELECT * FROM player WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(Player.from(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * find Players by team id
     *
     * @param teamId 팀 id
     * @return Matched List of Players by team id
     * @throws SQLException
     */
    public List<Player> findByTeamId(Long teamId) throws SQLException {

        String query = "SELECT * FROM player WHERE team_id = ?";

        List<Player> players = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, teamId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                players.add(Player.from(resultSet));
            }
        }

        return players;
    }
}
