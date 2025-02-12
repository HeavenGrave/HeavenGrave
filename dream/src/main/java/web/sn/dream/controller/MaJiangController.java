package web.sn.dream.controller;


import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.sn.dream.mapper.UserMapper;
import web.sn.dream.pojo.CardMaJiang;
import web.sn.dream.pojo.MaJiang;
import web.sn.dream.pojo.Result;
import web.sn.dream.service.MaJiangService;
import web.sn.dream.websoket.WebSocketService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 到三游戏接口文件
 * @author Stan
 * @version 1.0
 * @description: TODO
 * @date 2023/7/13 21:06
 */
@Slf4j
@RestController
@RequestMapping("mj")
public class MaJiangController{

    @Autowired
    private MaJiangService maJiangService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketService webSocketService;

    static  ArrayList<CardMaJiang> oneBox_new=new ArrayList<>();

    static{
        //创建一幅牌
        int index=1;
        //万 条 筒 风Dragon
        HashMap<String ,String> hm_decor=new HashMap<>();
        hm_decor.put("Characters","万");
        hm_decor.put("Bamboos","条");
        hm_decor.put("Dots","筒");
        hm_decor.put("Dragon","风");
        String [] nums={"1","2","3","4","5","6","7","8","9"};
        String [] types={"中","發","白"};
        for(String decor : hm_decor.keySet()){
            if(decor.equals("Dragon")){
                for(int i=0;i<types.length;i++) {
                    CardMaJiang card1=new CardMaJiang();
                    card1.setId(UUID.randomUUID().toString());
                    card1.setDecor(decor);
                    card1.setName(types[i]);
                    card1.setSize(i+1);
                    CardMaJiang card2=new CardMaJiang();
                    card2.setId(UUID.randomUUID().toString());
                    card2.setDecor(decor);
                    card2.setName(types[i]);
                    card2.setSize(i+1);
                    CardMaJiang card3=new CardMaJiang();
                    card3.setId(UUID.randomUUID().toString());
                    card3.setDecor(decor);
                    card3.setName(types[i]);
                    card3.setSize(i+1);
                    CardMaJiang card4=new CardMaJiang();
                    card4.setId(UUID.randomUUID().toString());
                    card4.setDecor(decor);
                    card4.setName(types[i]);
                    card4.setSize(i+1);
                    oneBox_new.add(card1);
                    oneBox_new.add(card2);
                    oneBox_new.add(card3);
                    oneBox_new.add(card4);
                }
            }else{
                for(int i=0;i<nums.length;i++){
                    CardMaJiang card1=new CardMaJiang();
                    card1.setId(UUID.randomUUID().toString());
                    card1.setDecor(decor);
                    card1.setName(nums[i]+hm_decor.get(decor));
                    card1.setSize(i+1);
                    CardMaJiang card2=new CardMaJiang();
                    card2.setId(UUID.randomUUID().toString());
                    card2.setDecor(decor);
                    card2.setName(nums[i]+hm_decor.get(decor));
                    card2.setSize(i+1);
                    CardMaJiang card3=new CardMaJiang();
                    card3.setId(UUID.randomUUID().toString());
                    card3.setDecor(decor);
                    card3.setName(nums[i]+hm_decor.get(decor));
                    card3.setSize(i+1);
                    CardMaJiang card4=new CardMaJiang();
                    card4.setId(UUID.randomUUID().toString());
                    card4.setDecor(decor);
                    card4.setName(nums[i]+hm_decor.get(decor));
                    card4.setSize(i+1);
                    oneBox_new.add(card1);
                    oneBox_new.add(card2);
                    oneBox_new.add(card3);
                    oneBox_new.add(card4);
                }
            }
        }
    }

    /**
     * 创建麻将游戏房间
     * @param session
     * @return
     */
    @RequestMapping("/create")
    public Result create(Integer userNum,HttpSession session){
        MaJiang maJiang =new MaJiang();
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // 格式化当前时间
        String formattedTime = currentTime.format(formatter);
        String[] nums=formattedTime.split(":");
        //将当前时间作为房间号,可以避免房间ID重复,也可以让房间号变的没有那么复杂   因为只使用了6位数字   需要一个月清除一次数据库   后续可以研究一个定时任务
        StringBuilder roomId= new StringBuilder();
        for(String num:nums){
            roomId.append(num);
        }
        //初始化游戏房间信息
        maJiang.setId(roomId.toString());
        maJiang.setCreateUserId(session.getAttribute("id").toString()); //创建人id
        maJiang.setCreateUserName(session.getAttribute("name").toString()); //创建人名称
        maJiang.setPlayer1(session.getAttribute("id").toString()); //创建人即为1号玩家
        maJiang.setPlayerNum(1); //当前对局加入的玩家数
        maJiang.setNowNum(userNum);
        //将当前数据插入数据库
        maJiangService.insertMaJiang(maJiang);
        //输出log信息
        System.out.println("用户："+session.getAttribute("name").toString()+"创建了房间："+roomId+"等待其他玩家进入。。。");
        //拼接post请求返回值
        Map<String, Object> data = new HashMap<>();
        data.put("roomId", roomId.toString());//房间ID
        data.put("maJiang",maJiang);//游戏信息
        return Result.success(data);

    }

    /**
     * 加入一场游戏
     * @param roomId
     * @param session
     * @return
     */
    @RequestMapping("/add")
    public Result add(String roomId,HttpSession session) {
        // 根据房间id查询游戏
        MaJiang maJiang = maJiangService.findMaJiangById(roomId);
        //输出log信息
        System.out.println("用户："+session.getAttribute("name").toString()+"加入了房间："+roomId);
        //当前玩家编号
        int nowNum = maJiang.getPlayerNum();
        //判断是否为新玩家
        Boolean ifNewPlayer=true;
        if(maJiang.getPlayer1().equals(session.getAttribute("id").toString())){
            ifNewPlayer=false;
        }else if(maJiang.getPlayer2()!=null&&maJiang.getPlayer2().equals(session.getAttribute("id").toString())){
            ifNewPlayer=false;
        }else if(maJiang.getPlayer3()!=null&&maJiang.getPlayer3().equals(session.getAttribute("id").toString())){
            ifNewPlayer=false;
        }else if(maJiang.getPlayer4()!=null&&maJiang.getPlayer4().equals(session.getAttribute("id").toString())){
            ifNewPlayer=false;
        }
        Map<String, Object> data = new HashMap<>();
        if(ifNewPlayer) {
            if (nowNum == 1) {
                maJiang.setPlayer2(session.getAttribute("id").toString());
            } else if (nowNum == 2) {
                maJiang.setPlayer3(session.getAttribute("id").toString());
            } else if (nowNum == 3) {
                maJiang.setPlayer4(session.getAttribute("id").toString());
            } else if (nowNum == 4) {
                data.put("type", "addGameError");
                data.put("maJiang", maJiang);
                return Result.success( data);
            }
            maJiang.setPlayerNum(nowNum + 1);
            //更新游戏数据
            boolean chek=maJiangService.updateMaJiang(maJiang);
            if(chek) {
                log.info(data.toString());
                data.put("type", "addGame");
                data.put("maJiang", maJiang);
                data.put("playerNum", maJiang.getPlayerNum());//当前玩家编号 座位号
                webSocketService.sendMessageToClient("/topic/messages", data);
            }else{
                System.out.println("用户："+session.getAttribute("name").toString()+"加入房间："+roomId+"失败！");
                return Result.error("加入房间失败");
            }
        }else{
            //拼接返回数据
            data.put("type", "addOldGame");
            data.put("maJiang", maJiang);
            //后续添加找回对局的逻辑
        }
        //data 用于当前账户的信息处理
        return Result.success(data);
    }

    /**
     * 房主开启游戏对局
     * @param roomId
     * @param session
     */
    @RequestMapping("/start")
    public void start(String roomId,HttpSession session) {
        //从session中获取当前用户名称
        String name =session.getAttribute("name").toString();
        //输出log信息
        System.out.println("用户："+name+" 作为房主开始了游戏  房间号为："+roomId);
        //根据房间id查询游戏信息
        MaJiang maJiang = maJiangService.findMaJiangById(roomId);
        //洗牌   将牌随机打乱
        Collections.shuffle(oneBox_new);
        List<CardMaJiang> cords1=new ArrayList<>();
        List<CardMaJiang> cords2=new ArrayList<>();
        List<CardMaJiang> cords3=new ArrayList<>();
        List<CardMaJiang> cords4=new ArrayList<>();
        List<CardMaJiang> cords_other=new ArrayList<>();
        int num=1; //玩家编号
        //将牌顺序分发给四个人
        for(int i=0;i<=(13*maJiang.getNowNum());i++){
            if(num==1){
                cords1.add(oneBox_new.get(i));
                num++;
            }else if(num==2){
                cords2.add(oneBox_new.get(i));
                num++;
            }else if(num==3){
                cords3.add(oneBox_new.get(i));
                if (maJiang.getNowNum()==4) {
                    num++;
                }else {
                    num=1;
                }
            }else if(num==4){
                cords4.add(oneBox_new.get(i));
                num=1;
            }
        }
        //将剩余的牌加入牌堆中
        for(int i=(13*maJiang.getNowNum()+1);i<oneBox_new.size();i++) {
            cords_other.add(oneBox_new.get(i));
        }
        HashMap<String,Object> json= new HashMap<>();
        json.put("type","distributedCard");//发牌
        json.put("player1",cords1);
        json.put("player2",cords2);
        json.put("player3",cords3);
        json.put("player4",cords4);
        json.put("other",cords_other);
        //发送websocket，告诉其他人开始游戏了
        webSocketService.sendMessageToClient("/topic/messages", json);
    }
//
//    /**
//     * 出牌或让步
//     * @param type 出牌 让步 喝风
//     * @param cards 出的牌
//     * @param num 玩家编号
//     * @param name 玩家名称
//     */
//    @RequestMapping("/outCard")
//    public void outCard(String type,String cards, String num,String name){
//
//        JSONObject json = new JSONObject();
//        //根据自定义转换的方法  将json信息 转化为卡牌JsonNode的数组
//        List<JsonNode> list_json = parseJson(cards);//上家出的牌
//        List<Card> list_card =new ArrayList<>();
//        String cardInfo="";
//       //通过遍历 将卡牌JsonNode数组信息转为为卡牌对象数组信息
//        for(JsonNode json1:list_json){
//            Card card=new Card();
//            if(json1.has("id")){
//                card.setId(json1.get("id").asInt());
//                card.setDecor(json1.get("decor").asText());
//                card.setSize(json1.get("size").asInt());
//                card.setNumber(json1.get("number").asText());
//                card.setLenght(json1.get("lenght").asInt());
//                list_card.add(card);
//                cardInfo+=card.getDecor()+" "+card.getNumber()+"; ";
//            }
//        }
//        //输出log信息
//        System.out.println("用户："+name+"出了："+list_card.size()+"张牌;  玩家编号为："+num+" 牌的信息如下:");
//        System.out.println(cardInfo);
//        //根据出牌类型进行操作
//        if(type.equals("outCard")){//当前为出牌
//            if(list_card.size()>0){//出牌数不为0
//                json.put("type","outNext");//下家请出牌
//                json.put("outCards",list_card);
//                json.put("outName",name);
//                //下家  玩家编号
//                if(Integer.parseInt(num)+1>5){
//                    json.put("nextNum",1);
//                }else {
//                    json.put("nextNum", Integer.parseInt(num) + 1);
//                }
//            }else { //出牌数为0  即为让步  一般用于已经出完牌的玩家
//                json.put("type","outNext");//下家请出牌
//                json.put("outCards",list_card);
//                json.put("outName",name);
//                json.put("nextNum",Integer.parseInt(num));
//            }
//        }else if(type.equals("passCard") ){ //让步
//            json.put("type","outNext");
//            json.put("outCards",list_card);
//            json.put("outName",name);
//            //下家  玩家编号
//            if(Integer.parseInt(num)+1>5){
//                json.put("nextNum",1);
//            }else {
//                json.put("nextNum", Integer.parseInt(num) + 1);
//            }
//        }else {//喝风
//            json.put("type","outNext");
//            json.put("outCards",list_card);
//            //根据房间id获取游戏数据
//            maJiang maJiang = maJiangService.findDaosnById(type);
//            //获取当前玩家的编号
//            if(maJiang.getPlayer1().equals(name)){
//                json.put("outName",maJiang.getPlayer2());
//            }else if(maJiang.getPlayer2().equals(name)){
//                json.put("outName",maJiang.getPlayer3());
//            }else if(maJiang.getPlayer3().equals(name)){
//                json.put("outName",maJiang.getPlayer4());
//            }else if(maJiang.getPlayer4().equals(name)){
//                json.put("outName",maJiang.getPlayer5());
//            }else if(maJiang.getPlayer5().equals(name)){
//                json.put("outName",maJiang.getPlayer1());
//            }
//            //下家  玩家编号
//            if(Integer.parseInt(num)+1>5){
//                json.put("nextNum",1);
//            }else {
//                json.put("nextNum", Integer.parseInt(num) + 1);
//            }
//        }
//
//        try {
//            //发送websocket，告诉其他人出了什么牌
//            WebSocketServer.sendInfo(json.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * 亮个三
//     * @param type 3的花色
//     * @param num 用户座位号
//     * @param name 用户名称
//     */
//    @RequestMapping("/lightSan")
//    public void lightSan(String type,String num,String name){
//        JSONObject json = new JSONObject();
//        //输出log信息
//        System.out.println("用户："+name+"亮了："+type+"三  座位号为："+num);
//        json.put("type","lightSan");//操作类型
//        json.put("decor",type);//花色
//        json.put("player",name);//亮三用户
//        json.put("num",num);//座位号
//        try {
//            //发送webSocket信息 广播亮三的信息
//            WebSocketServer.sendInfo(json.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 格式化json
//     * @param jsonData
//     * @return
//     */
//    public static List<JsonNode> parseJson(String jsonData) {
//        List<JsonNode> result = new ArrayList<>();
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//            JsonNode data = mapper.readTree(jsonData);
//            for (JsonNode group : data) {
//                JsonNode entry = mapper.createObjectNode();
//                // 提取属性值
//                ((ObjectNode)entry).put("id", group.get("id").asInt());
//                ((ObjectNode)entry).put("number", group.get("number").asText());
//                ((ObjectNode)entry).put("decor", group.get("decor").asText());
//                ((ObjectNode)entry).put("size", group.get("size").asInt());
//                ((ObjectNode)entry).put("lenght", group.get("lenght").asInt());
//                result.add(entry);
//            }
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//
//
//    /**
//     * 出完了
//     * @param num
//     * @param name
//     */
//    @RequestMapping("/win")
//    public void Win(String num,String name,String type){
//        JSONObject json = new JSONObject();
//        //输出log信息
//        System.out.println("用户："+name+"出完了； 类型："+type+"  玩家编号："+num);
//        json.put("type","PlayerWin");
//        json.put("player",name);
//        json.put("playerNum",num);
//        json.put("playerType",type);
//        try {
//            WebSocketServer.sendInfo(json.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 更新用户积分
//     * @param result
//     */
//    @RequestMapping("/updateUserScore")
//    public JsonResult<Map<String, Object>> Win(int result,String name){
//        User user = userMapper.findUserByName(name);
//        user.setScore(user.getScore()+result);
//        //更新用户信息
//        userMapper.updateUser(user);
//        System.out.println("用户："+name+"更新了积分; 当前的积分为："+(user.getScore()));
//        return  new JsonResult<>(OK);
//    }

}


