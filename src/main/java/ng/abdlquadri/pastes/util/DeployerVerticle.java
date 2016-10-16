/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.abdlquadri.pastes.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import ng.abdlquadri.pastes.EntryVerticle;

/**
 *
 * @author abdlquadri
 */
public class DeployerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {

        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config()).setWorker(true);

        vertx.deployVerticle(new EntryVerticle(), deploymentOptions);


    }

}
