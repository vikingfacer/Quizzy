import json

from peewee import DoesNotExist, IntegrityError
from flask import Blueprint, request, Response

from App.Models.Users import User

user = Blueprint('user', __name__)


def format_json(content):
    '''
        this function is to keep all the json string data nice and uniform
         ALL LOWER CASE
    '''
    formated = {}
    new_key = ""
    new_value = ""
    for key, value in content.items():

        if type(key) == str:
            new_key = key.lower()
        else:
            new_key = key

        if type(value) == str:
            new_value = value.lower()
        else:
            new_value = value

        formated.update({new_key: new_value})

    return formated

@user.route('/user', methods=['POST'])
def create_user():
    content = request.get_json()

    content = format_json(content)

    new_user = User()
    new_user.username = content.get("username")
    new_user.password = content.get("password")
    new_user.logged_in= True
    try:
        new_user.save()
    except IntegrityError:
        return Response(status=409)

    return json.dumps(new_user.to_dict())



@user.route('/user/login', methods=['POST'])
def login_user():
    # this is not safe at all I know it will be improved if time allows 
    content = format_json(request.get_json())

    if 'username' in list(content.keys()) or 'password' in list(content.keys()):
        try:
            got_user = User.get(User.username == str(content['username']))

            if got_user.password == content['password'] and got_user.logged_in != True:

                got_user.logged_in = True
                got_user.save()
                return json.dumps(got_user.to_dict())
            else:
                return Response(status = 203)

        except DoesNotExist:
            return Response(status = 404)

    return Response(status=404)



@user.route('/user', methods=['GET'])
def get_all_users():
    return json.dumps([user.to_dict() for user in User.select()])

@user.route('/user/<id>', methods=['GET'])
def get_by_id(id):
    try:
        got_user = User.get(User.id == id)
        return json.dumps(got_user.to_dict())

    except DoesNotExist:
        return Response(status = 404)

@user.route('/user/<key>=<value>', methods=['GET'])
def get_user_by_name(key, value):
    key = str(key).lower()

    jsonDict = {}
    got_user = None

    if key == 'id':
        try:
            got_user = User.get(User.id == value)
            jsonDict = got_user.to_dict()
            return json.dumps( got_user.to_dict())

        except DoesNotExist:
            return Response(status=404)

    elif key == 'username':
        try:
            got_user = User.get(User.username == value)
            jsonDict = got_user.to_dict()
            return json.dumps( got_user.to_dict())

        except DoesNotExist:
            return Response(status=404)        

    return Response(status = 404)


@user.route('/user/<id>', methods=['PUT'])
def update_user(id):
    # this will probably need changed for ease of use
    content = format_json(request.get_json())

    if "newpassword" in content.keys() and "password" in content.keys():
        try:
            update_user = User.get(User.id == id)
            if update_user.Password == content['password']:
                update_user.Password = content['newpassword']

                # this save may need a try/catch
                update_user.save()

                return json.dumps({'id' : id})
            else:
                return Response(status = 418)

        except DoesNotExist:
            return Response(status = 404)

    return Response(status=404)


@user.route('/user/<id>', methods=['DELETE'])
@user.route('/user/<key>=<value>', methods=['DELETE'])
def delete_user(id =None, key = None, value = None):

    content = format_json(request.get_json())
    keys = ['email', 'username']

    if id != None:
        try:
            got_user = User.get(User.id == id)

        except DoesNotExist:
            return Response(status = 409)

        if got_user.Password == content['password']:
            try:
                got_user.delete_instance()
                return Response(status = 200)

            except IntegrityError:
                return Response(status=409)  

    elif str(key).lower() in keys:
        try:
            got_user = None

            if str(key).lower() == 'email':
                got_user = User.get(User.Email == value)

            elif str(key).lower() == 'username':
                got_user = User.get(User.Username == value)

        except DoesNotExist:
            return Response(status = 409)

        if got_user.Password == content['password']:
            try:
                got_user.delete_instance()
                return Response(status = 200)

            except IntegrityError:
                return Response(status=409)  
        
        else:
            return Response(status = 203)

    return Response(status = 404)
