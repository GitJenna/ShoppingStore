app.controller('payController' ,function($scope ,$location,payService,$http){
	
	
	$scope.createNative=function(){
		payService.createNative().success(
			function(response){
				
				//显示订单号和金额
				$scope.money= (response.total_fee/100).toFixed(2);
				$scope.out_trade_no=response.out_trade_no;
				
				//生成二维码
				 var qr=new QRious({
					    element:document.getElementById('qrious'),
						size:250,
						value:response.code_url,
						level:'H'
			     });
				 
				 queryPayStatus();//调用查询
				
			}	
		);	
	}
	
	//调用查询
	queryPayStatus=function(){
		payService.queryPayStatus($scope.out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{
					if(response.message=='二维码超时'){
						//$scope.createNative();//重新生成二维码
						alert("二维码超时");
                        location.href="payfail.html";
					}else{
						location.href="payfail.html";
					}
				}				
			}		
		);		
	}
	
	//获取金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}




    //提交订单
    $scope.submitOrder=function(){
        alert($location.search()['seckillId']);
        $http.get('seckillOrder/submitOrder.do?seckillId='+$location.search()['seckillId']).success(
            function(response){
                if(response.success){//如果下单成功
                    alert("抢购成功，请在5分钟之内完成支付");
                    //location.href="pay.html";//跳转到支付页面
                }else{
                    alert(response.message);
                }
            }
        );

    }
	
});