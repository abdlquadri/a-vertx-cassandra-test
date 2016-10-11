/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.abdlquadri.pastes.util;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.crypto.hash.Sha512Hash;

/**
 *
 * @author abdlquadri
 */
public class Util {


    public static JsonObject paramsToJSON(MultiMap params) {
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<String, String> param : params) {
            try {
                jsonObject.put(param.getKey(), URLDecoder.decode(param.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return jsonObject;
    }
}
