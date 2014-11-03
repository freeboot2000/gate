/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.gate.controllers

import com.netflix.spinnaker.gate.services.ClusterService
import com.netflix.spinnaker.gate.services.LoadBalancerService
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult


import static com.netflix.spinnaker.gate.controllers.AsyncControllerSupport.defer

@CompileStatic
@RequestMapping("/applications/{application}/clusters")
@RestController
class ClusterController {

  @Autowired
  ClusterService clusterService

  @Autowired
  LoadBalancerService loadBalancerService

  @RequestMapping(method = RequestMethod.GET)
  DeferredResult<Map> getClusters(@PathVariable("application") String app) {
    defer clusterService.getClusters(app)
  }

  @RequestMapping(value = "/{account}", method = RequestMethod.GET)
  DeferredResult<List<Map>> getClusters(
      @PathVariable("application") String app, @PathVariable("account") String account) {
    defer clusterService.getClustersForAccount(app, account).toList()
  }

  @RequestMapping(value = "/{account}/{clusterName}", method = RequestMethod.GET)
  DeferredResult<Map> getClusters(@PathVariable("application") String app,
                                  @PathVariable("account") String account,
                                  @PathVariable("clusterName") String clusterName) {
    defer clusterService.getCluster(app, account, clusterName)
  }

  @RequestMapping(value = "/{account}/{clusterName}/serverGroups", method = RequestMethod.GET)
  DeferredResult<List> getServerGroups(@PathVariable("application") String app,
                                       @PathVariable("account") String account,
                                       @PathVariable("clusterName") String clusterName) {
    defer clusterService.getClusterServerGroups(app, account, clusterName)
  }

  @CompileStatic(TypeCheckingMode.SKIP)
  @RequestMapping(value = "/{account}/{clusterName}/serverGroups/{serverGroupName}", method = RequestMethod.GET)
  DeferredResult<Map> getServerGroups(@PathVariable("application") String app,
                                      @PathVariable("account") String account,
                                      @PathVariable("clusterName") String clusterName,
                                      @PathVariable("serverGroupName") String serverGroupName) {
    DeferredResult<Map> q = new DeferredResult<>()
    // TODO this crappy logic needs to be here until the "type" field is removed in Oort
    clusterService.getClusterServerGroups(app, account, clusterName).subscribe({ serverGroups ->
      q.setResult(serverGroups.find {
        it.name == serverGroupName
      })
    }, { Throwable t ->
      q.setErrorResult(t)
    })
    q
  }

  @RequestMapping(value = "/{account}/{clusterName}/tags", method = RequestMethod.GET)
  DeferredResult<List<String>> getClusterTags(@PathVariable("clusterName") String clusterName) {
    defer clusterService.getClusterTags(clusterName)
  }
}
