This is a sample for the taste docker.

You have to mount this dir and exec the run-taste.sh script in the docker :

```
sudo docker run -ti -v ...../docker/sample:/mnt taste /run-taste.sh -d /mnt/target -c /mnt/cfg.taste /mnt/tests.taste
```
