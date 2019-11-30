package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net"
	"os"
	"time"
)

const MARK string = "MARK"
const LOOPMESSAGE string = "m"
const CMDSNAPSHOT string = "m"
const CMDCLEAR string = "c"

type Client struct {
	ID           string          `json:"id"`
	LocalAddress string          `json:"localAddress"`
	Others       []*MyConnection `json:"others"`
	id2MyConn    map[string]*MyConnection
}

type MyConnection struct {
	amount        int
	ID            string `json:"channelId"`
	RemoteAddress string `json:"remoteAddress"`
	record        bool
}

func (th *Client) init(fileanme string) error {
	fd, err := ioutil.ReadFile(fileanme)
	if err != nil {
		return err
	}
	err = json.Unmarshal(fd, &th)
	if err != nil {
		return err
	}
	th.id2MyConn = make(map[string]*MyConnection)
	return nil
}

func (th *MyConnection) snapshot() {
	log.Printf("sanpshot %s->%s： %d\n", th.ID, th.RemoteAddress, th.amount)
}

func (th *Client) loop() {
	server, err := net.Listen("tcp", th.LocalAddress)
	if err != nil {
		log.Fatalln(err.Error())
		return
	}
	defer server.Close()
	for {
		conn, err := server.Accept()
		defer conn.Close()
		if err != nil {
			log.Printf("cannot accept connection: %v", err)
		}
		go func(conn net.Conn) {
			// 服务器另起例程处理消息
			defer conn.Close()
			data := make([]byte, 512)
			n, err := conn.Read(data) // 初次通话先报上名来
			userID := string(data[0:n])
			myconn, ok := th.id2MyConn[userID]
			if !ok {
				myconn = &MyConnection{101, string(data[0:n]), conn.RemoteAddr().String(), false}
				th.id2MyConn[userID] = myconn
			}
			for {
				n, err2 := conn.Read(data)
				if err2 != nil {
					log.Fatalln(err.Error())
					return
				}
				message := string(data[:n])
				log.Println("l->r", myconn.ID, conn.LocalAddr(), conn.RemoteAddr(), myconn.amount, message, myconn.record)
				switch message {
				case MARK:
					if myconn.record == false {
						myconn.snapshot()
						myconn.record = true
						conn.Write([]byte(MARK))
						return
					}
					break
				case LOOPMESSAGE:
					myconn.amount++
					conn.Write([]byte(LOOPMESSAGE))
					break
				case CMDCLEAR:
					if myconn.record == true {
						myconn.record = false
						conn.Write([]byte(CMDCLEAR))
						return
					}
					break
				default:
					log.Fatalln("未匹配的信息", data)
				}
				time.Sleep(1 * time.Second)
			}
		}(conn)
	}
}

func (th *MyConnection) SendData(data []byte) error {
	// 客户端发送数据到远程地址
	conn, err := net.Dial("tcp", th.RemoteAddress)
	if err != nil {
		log.Fatalln(err.Error())
		return err
	}
	defer conn.Close()
	conn.Write([]byte(th.ID))
	time.Sleep(1e8 * time.Nanosecond) // 等带上面消息发送完毕
	for {
		_, err = conn.Write(data)
		if err != nil { // 这里可以优化
			log.Fatalln(err.Error())
			return err
		}
		n, _ := conn.Read(data)
		message := string(data[:n])
		log.Println("l->r", th.ID, conn.LocalAddr(), conn.RemoteAddr(), th.amount, message, th.record)
		switch message {
		case LOOPMESSAGE:
			th.amount++
			data = []byte(LOOPMESSAGE)
			break
		case MARK:
			if th.record == false {
				th.record = true
				th.snapshot()
				data = []byte(MARK)
				conn.Write(data)
			}
			return nil
		case CMDCLEAR:
			th.record = false
			return nil
		default:
			log.Println("其他消息", message)
			return nil
		}
	}
	return nil
}

func main() {
	var client1 Client
	err1 := client1.init("client1.json")
	if err1 != nil {
		log.Fatalln(err1.Error())
	}
	var client2 Client
	err2 := client2.init("client2.json")
	if err2 != nil {
		log.Fatalln(err2.Error())
	}
	myconn1 := MyConnection{101, client1.Others[0].ID, client1.Others[0].RemoteAddress, false}
	go client2.loop()
	time.Sleep(1 * time.Second) // 等待client2服务器启动
	go myconn1.SendData([]byte(LOOPMESSAGE))
	fmt.Println("请输入m抓取快照；c清除记录：")
	for {
		in := bufio.NewReader(os.Stdin)
		cmd, _ := in.ReadString('\n')
		switch cmd[0 : len(cmd)-1] {
		case CMDSNAPSHOT:
			myconn1.SendData([]byte(MARK))
			break
		case CMDCLEAR:
			myconn1.record = false
			myconn1.SendData([]byte(CMDCLEAR))
			break
		default:
			fmt.Print("无效的指令，请输入m或c：")
		}
	}
}
