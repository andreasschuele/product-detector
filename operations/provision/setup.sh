echo -e "\n\
================================\n\
Install some tools ...\n\
================================\n\
\n"

sudo apt-get update

sudo apt-get install -y \
   net-tools \
   curl \
   wget \
   mc \
   nano \
   tree \
   htop \
   iftop \
   nethogs \
   bmon


echo -e "\n\
================================\n\
Install Docker ...\n\
================================\n\
\n"

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"

sudo apt-get update

sudo apt-get -y install docker-ce

sudo curl -L https://github.com/docker/compose/releases/download/1.21.2/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

echo -e "\n\
================================\n\
Setup PostgreSQL container ...\n\
================================\n\
\n"

sudo docker network create product-detector-net

sudo mkdir -p /docker_data/db-postgres

sudo docker create \
    --name db-postgres \
    --network product-detector-net \
    --env POSTGRES_DB=product_detector \
    --env POSTGRES_USER=product_detector \
    --env POSTGRES_PASSWORD=product_detector \
    --env PGDATA=/data/pgdata \
    --publish 5432:5432 \
    -v /docker_data/db-postgres:/data \
    postgres

sudo docker start db-postgres

