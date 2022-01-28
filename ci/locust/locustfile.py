import string
import random
from json import JSONDecodeError

from locust import HttpUser, task


class XURLUser(HttpUser):

    @task(1)
    def health_check(self):
        with self.client.get("/_health", catch_response=True) as response:
            try:
                if response.status_code != 200:
                    response.failure("health check endpoint failed")
                else:
                    body = response.json()
                    if not body['storage']:
                        response.failure("storage health failed")
                    elif not body['cache']:
                        response.failure("cache health failed")
                    else:
                        response.success()
            except JSONDecodeError:
                response.failure("json error")
            except KeyError:
                response.failure("key error")
            
    
    @task(2)
    def create(self):
        with self.client.post(
            "/api/v1/urls",
            json={"url": "https://httpbin.org/get?from=locust"},
            catch_response=True) as response:
        
            if response.status_code == 201:
                response.success()
            else:
                response.failure("create failed")

    @task(3)
    def redirect(self):
        key = random.choice(string.ascii_letters)
        with self.client.get(f"/{key}", catch_response=True) as response:
            if response.status_code in [301, 404, 200]:
                response.success()
            else:
                response.failure(f"redirect failed")

    @task(1)
    def list(self):
        self.client.get("/api/v1/urls")

    @task(1)
    def detail(self):
        key = random.choice(string.ascii_letters)
        with self.client.get(f"/api/v1/urls/{key}", catch_response=True) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure("detail failed")
