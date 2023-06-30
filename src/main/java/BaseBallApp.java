import annotation.RequestMapping;
import dao.OutPlayerDAO;
import dao.PlayerDAO;
import dao.StadiumDAO;
import dao.TeamDAO;
import db.DBConnection;
import domain.Request;
import dto.player.OutPlayerDTO;
import dto.player.PlayerDTO;
import dto.stadium.StadiumRequest;
import dto.stadium.StadiumResponse;
import dto.team.TeamRequest;
import dto.team.TeamResponse;
import dto.team.TeamWithStadiumResponse;
import exception.*;
import service.OutPlayerService;
import service.PlayerService;
import service.StadiumService;
import service.TeamService;
import view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;

public class BaseBallApp {

    private static final StadiumService STADIUM_SERVICE;
    private static final TeamService TEAM_SERVICE;
    private static final PlayerService PLAYER_SERVICE;
    private static final OutPlayerService OUT_PLAYER_SERVICE;

    // inject dependency
    static {
        Connection connection = DBConnection.getInstance();

        StadiumDAO stadiumDAO = new StadiumDAO(connection);
        TeamDAO teamDAO = new TeamDAO(connection);
        PlayerDAO playerDAO = new PlayerDAO(connection);
        OutPlayerDAO outPlayerDAO = new OutPlayerDAO(connection);

        STADIUM_SERVICE = new StadiumService(stadiumDAO);
        TEAM_SERVICE = new TeamService(teamDAO, stadiumDAO);
        PLAYER_SERVICE = new PlayerService(playerDAO);
        OUT_PLAYER_SERVICE = new OutPlayerService(outPlayerDAO, playerDAO, connection);
    }

    public static void main(String[] args) {
        while (true) {
            try {
                Request request = View.inputRequest();
                mappingRequest(request);
            } catch (IllegalAccessException | InvocationTargetException | BadRequestException exception) {
                View.printErrorMessage(exception.getCause().toString());
            }
        }
    }

    public static void mappingRequest(final Request request)
            throws IllegalAccessException, InvocationTargetException, BadRequestException {

        Method[] methods = BaseBallApp.class.getDeclaredMethods();

        for (Method method : methods) {
            RequestMapping requestMapping = method.getDeclaredAnnotation(RequestMapping.class);

            if (Objects.isNull(requestMapping)) continue;

            if (Objects.equals(requestMapping.request(), request.getHeader())) {
                method.invoke(BaseBallApp.class, request);
                return;
            }
        }
        throw new BadRequestException("요청 형식이 올바르지 않습니다.");
    }

    @RequestMapping(request = "야구장등록")
    private static void saveStadium(final Request request) {
        try {
            StadiumRequest stadiumRequest = StadiumRequest.from(request);
            StadiumResponse response = STADIUM_SERVICE.save(stadiumRequest);
            View.printResponse(response);

        } catch (StadiumRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "야구장목록")
    private static void viewAllStadiums(final Request request) {
        try {
            if (!Objects.isNull(request.getBody())) throw new BadRequestException();

            List<StadiumResponse> allStadiums = STADIUM_SERVICE.findAll();
            View.printResponse(allStadiums);

        } catch (StadiumFindFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "팀등록")
    private static void saveTeam(final Request request) {
        try {
            TeamRequest teamRequest = TeamRequest.from(request);
            TeamResponse response = TEAM_SERVICE.save(teamRequest);
            View.printResponse(response);

        } catch (TeamRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "팀목록")
    private static void viewAllTeams(final Request request) {
        try {
            if (!Objects.isNull(request.getBody())) throw new BadRequestException();
            List<TeamWithStadiumResponse> allTeamWithStadium = TEAM_SERVICE.findAllWithStadium();
            View.printResponse(allTeamWithStadium);

        } catch (TeamFindFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "선수목록")
    private static void viewPlayersByTeam(final Request request) {
        try {
            PlayerDTO.FindPlayersByTeamRequest findPlayersByTeamRequest = PlayerDTO.FindPlayersByTeamRequest.from(request);
            List<PlayerDTO.FindPlayerResponse> response = PLAYER_SERVICE.findByTeam(findPlayersByTeamRequest);
            View.printResponse(response);

        } catch (StadiumRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "퇴출목록")
    private static void viewOutPlayers(final Request request) {
        try {
            List<OutPlayerDTO.FindOutPlayerResponse> response = OUT_PLAYER_SERVICE.findOutPlayers();
            View.printResponse(response);

        } catch (StadiumRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "포지션별목록")
    private static void viewPlayersGroupByPosition(final Request request) {
        try {
            List<PlayerDTO.FindPlayerGroupByPositionResponse> response = PLAYER_SERVICE.findPlayerGroupByPosition();
            View.printResponseAsPivot(response, "teamName", "position", "name");

        } catch (StadiumRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "선수등록")
    private static void savePlayer(final Request request) {
        try {
            PlayerDTO.NewPlayerRequest newPlayerRequest = PlayerDTO.NewPlayerRequest.from(request);
            PlayerDTO.FindPlayerResponse response = PLAYER_SERVICE.save(newPlayerRequest);
            View.printResponse(response);

        } catch (TeamRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }

    @RequestMapping(request = "퇴출등록")
    private static void saveOutPlayer(final Request request) {
        try {
            OutPlayerDTO.NewOutPlayerRequest newOutPlayerRequest = OutPlayerDTO.NewOutPlayerRequest.from(request);
            OutPlayerDTO.FindOutPlayerResponse response = OUT_PLAYER_SERVICE.save(newOutPlayerRequest);
            View.printResponse(response);

        } catch (TeamRegistrationFailureException | BadRequestException exception) {
            View.printErrorMessage(exception.getMessage());
        }
    }
}
