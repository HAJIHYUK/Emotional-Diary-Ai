<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>카카오맵 테스트</title>
    <script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=56e30925755c1f998dff7c17420d70ee&libraries=services"></script>
    <style>
        #map {width:100%; height:350px; margin-top:10px;}
    </style>
</head>
<body>
    <input type="text" id="addressInput" placeholder="주소를 입력하세요">
    <button type="button" onclick="searchAddress()">검색</button>
    <div id="map"></div>
    <input type="text" id="selectedLocation" readonly placeholder="전체 주소가 여기에 표시됩니다">
    <button type="button" onclick="saveLocation()">OK</button>

    <!-- 내 스크립트는 body 맨 마지막에! -->
    <script>
    let map;
    let marker;

    function initMap() {
        const container = document.getElementById('map');
        const options = {
            center: new kakao.maps.LatLng(37.5665, 126.9780),
            level: 3
        };
        map = new kakao.maps.Map(container, options);
    }

    function searchAddress() {
        const address = document.getElementById('addressInput').value;
        const geocoder = new kakao.maps.services.Geocoder();

        geocoder.addressSearch(address, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                map.setCenter(coords);
                if (marker) marker.setMap(null);
                marker = new kakao.maps.Marker({
                    map: map,
                    position: coords
                });
                // 전체 주소 저장 (도로명 주소가 있으면 우선, 없으면 지번주소)
                let fullAddress = result[0].road_address ? result[0].road_address.address_name : result[0].address.address_name;
                document.getElementById('selectedLocation').value = fullAddress;
            } else {
                alert('주소를 찾을 수 없습니다.');
            }
        });
    }

    function saveLocation() {
        const location = document.getElementById('selectedLocation').value;
        if (location) {
            fetch('/api/user-data/savelocation', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'location=' + encodeURIComponent(location)
            })
            .then(response => response.json())
            .then(data => {
                alert('서버 응답: ' + data.message);
                // 성공 후 원하는 동작 추가
            })
            .catch(error => {
                alert('에러 발생: ' + error);
            });
        } else {
            alert('먼저 주소를 검색하고 위치를 선택하세요.');
        }
    }

    window.onload = initMap;
    </script>
</body>
</html>
