package com.ppjun.game.controller

import com.ppjun.game.base.Constant
import com.ppjun.game.entity.GameInfo
import com.ppjun.game.entity.Response
import com.ppjun.game.service.*
import com.ppjun.game.util.MD5Util
import com.ppjun.game.util.TimeUtil.Companion.getCurrentTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat


@RestController
@EnableAutoConfiguration
class GameController {

    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var deviceService: DeviceService

    @Autowired
    lateinit var payService: PayService

    @Autowired
    lateinit var adminService: AdminService


    /**
     * 获取全部游戏,通过admin的token获取
     */

    @PostMapping("/game/page")
    fun getGameByPage(@RequestParam map: HashMap<String, String>): Response {

        val token = map["user_token"]
        val page = map["page"]
        if (token.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "token 为空", "")
        }
        if (page.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "page 为空", "")
        }
        val adminList = adminService.getAdminByToken(token!!)
        if (adminList.isEmpty()) {
            return Response(Constant.ERROR_CODE, "找不到游戏", "")
        }
        val gameListPair = gameService.getGameByPage(page!!.toInt())
        return if (gameListPair.first.isEmpty()) {
            Response(Constant.SUCCESS_CODE, "无游戏", "")
        } else {
            val pageMap = HashMap<String, Any>()
            pageMap["list"] = gameListPair.first
            pageMap["page"] = gameListPair.second
            Response(Constant.SUCCESS_CODE, "成功", pageMap)
        }
    }

    @PostMapping("/game/all")
    fun allGame(@RequestParam map: HashMap<String, String>): Response {

        val token = map["user_token"]

        if (token.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "token 为空", "")
        }

        val adminList = adminService.getAdminByToken(token!!)
        if (adminList.isEmpty()) {
            return Response(Constant.ERROR_CODE, "找不到游戏", "")
        }
        val gameList = gameService.getAllGame()
        return if (gameList.isEmpty()) {
            Response(Constant.SUCCESS_CODE, "无游戏", "")
        } else {
            Response(Constant.SUCCESS_CODE, "成功", gameList)
        }
    }


    @PostMapping("/game/add")
    fun addGame(@RequestParam map: HashMap<String, String>)
            : Response {
        val gameName = map["game_name"]
        if (gameName.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "gameName 不能为空", "")
        }
        val token = map["user_token"]
        if (token.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "token 为空", "")
        }
        val adminList = adminService.getAdminByToken(token!!)
        if (adminList.isEmpty()) {
            return Response(Constant.ERROR_CODE, "找不到游戏", "")
        }
        val appId = MD5Util.getMD5(gameName)
        val appKey = MD5Util.getMD5(MD5Util.getMD5(gameName))

        if (gameService.getGameByName(requireNotNull(gameName)).isNotEmpty()) {
            return Response(Constant.ERROR_CODE, Constant.ERROR_REPECT_ADD, "")
        }

        gameService.insertGame(GameInfo(1, requireNotNull(gameName), requireNotNull("appId$appId"),
                requireNotNull("appKey$appKey"), getCurrentTime()))
        return Response(Constant.SUCCESS_CODE, Constant.SUCCESS_ADD, "")
    }


    @PostMapping("/game/delete")
    fun deleteGame(@RequestParam map: HashMap<String, String>): Response {

        val gId = map["id"]
        if (gId.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "id 不能为空", "")
        }
        val token = map["user_token"]
        if (token.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "token 为空", "")
        }
        val adminList = adminService.getAdminByToken(token!!)
        if (adminList.isEmpty()) {
            return Response(Constant.ERROR_CODE, "找不到游戏", "")
        }
        //删除其他表数据，再删除game表
        return deleteGame(requireNotNull(gId))
    }

    @Transactional
    fun deleteGame(gameId: String):Response {
        //删除user表gameid
        //删除device表gameid
        //删除pay表gameid
        userService.deleteUserByGameId(gameId)
        deviceService.deleteDeviceByGameId(gameId)
        payService.deletePayInfoByGameId(gameId)
        gameService.deleteGame(gameId)

        return Response(Constant.SUCCESS_CODE, "删除成功","")

    }

    @PostMapping("/game/modify")
    fun modifyGame(@RequestParam map: HashMap<String, String>): Response {
        val gId = map["id"]
        val gameName = map["game_name"]
        if (gId.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "id 不能为空", "")
        }
        if (gameName.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "gameName 不能为空", "")
        }
        val token = map["user_token"]
        if (token.isNullOrEmpty()) {
            return Response(Constant.ERROR_CODE, "token 为空", "")
        }
        val adminList = adminService.getAdminByToken(token!!)
        if (adminList.isEmpty()) {
            return Response(Constant.ERROR_CODE, "找不到游戏", "")
        }
        val size = gameService.modifyGame(gId!!, gameName!!, getCurrentTime())
        return Response(Constant.SUCCESS_CODE, "修改成功", size)
    }


}