package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;


import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>(){{
        AddressDTO address = new AddressDTO();
        address.setId(1L);
        address.setAddress("重庆市巫溪县");
        address.setCity("重庆市");
        address.setDistrict("巫溪县");
        address.setName("lxf");
        address.setPhone("18756297530");
        address.setState("重庆市");
        address.setZipCode("21000");
        address.setIsDefault(true);
        add(address);

        AddressDTO address1 = new AddressDTO();
        address1.setId(2L);
        address1.setAddress("天堂路 3号楼");
        address1.setCity("北京");
        address1.setDistrict("朝阳区");
        address1.setName("张三");
        address1.setPhone("18866297530");
        address1.setState("北京");
        address1.setZipCode("100000");
        address1.setIsDefault(false);
        add(address1);
    }
    };

    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if(addressDTO.getId() == id){
                return addressDTO;
            }
        }
        return null;
    }


}
