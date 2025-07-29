package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guochang.aicodegenmicroservice.model.entity.User;
import generator.service.UserService;
import generator.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 31179
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-29 16:01:44
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




